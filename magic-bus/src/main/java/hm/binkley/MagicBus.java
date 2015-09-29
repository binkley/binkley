package hm.binkley;

import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * {@code MagicBus} is an intraprocess message bus.  Subscribers call {@link
 * #subscribe(Class, Mailbox)} to register mailboxes for receiving messages.
 * Senders call {@link #publish(Object)} to send messages.
 * <p>
 * Delivery is synchronous.  Order of delivery is: <ol><li>Base type
 * subscribers before subtype subscribers &mdash; Subscribers to {@code
 * Object.class} before all others, subscribers to most specific type
 * last</li> <li>Earlier subscribers before later subscribers.  Ensuring
 * synchronous subscription order is the responsibility of the
 * caller</li></ol>
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
@RequiredArgsConstructor
public final class MagicBus {
    /**
     * Discards messages for {@link #returned} and {@link #failed}, a
     * convenience.
     */
    public static <T> Consumer<T> discard() {
        return m -> {};
    }

    private final Subscribers subscribers = new Subscribers();
    /** Receives unsubscribed messages. */
    private final Consumer<? super UnsubscribedMessage> returned;
    /** Receives failed messages. */
    private final Consumer<? super FailedMessage> failed;

    /**
     * Subscribes the given <var>mailbox</var> for messages of
     * <var>messageType</var> and subtypes.
     *
     * @param messageType the message type token, never missing
     * @param mailbox the mailbox for delivery, never missing
     * @param <T> the message type
     */
    public <T> void subscribe(@Nonnull final Class<T> messageType,
            @Nonnull final Mailbox<? super T> mailbox) {
        subscribers.subscribe(messageType, mailbox);
    }

    /**
     * Unsubscribes the given <var>mailbox</var> for messages of
     * <var>messageType</var> and subtypes.
     *
     * @param messageType the message type token, never missing
     * @param mailbox the mailbox for delivery, never missing
     * @param <T> the message type
     */
    public <T> void unsubscribe(@Nonnull final Class<T> messageType,
            @Nonnull final Mailbox<? super T> mailbox) {
        subscribers.unsubscribe(messageType, mailbox);
    }

    /**
     * Publishes a message.  Subscribers to the type of <var>message</var> and
     * its supertypes recieve the message in their mailboxes.
     * <p>
     * If there is no eligible mailbox, <var>message</var> is sent to the
     * "unsubscribed messages" (dead letter) box.
     * <p>
     * If publishing fails (a mailbox throws a checked exception),
     * <var>message</var> is sent to the "failed message" box.
     *
     * @param message the message, never missing
     */
    public void publish(@Nonnull final Object message) {
        try (final Stream<Mailbox> mailboxes = subscribers.of(message)) {
            final AtomicInteger deliveries = new AtomicInteger();
            mailboxes.
                    onClose(() -> returnIfDead(deliveries, message)).
                    peek(record(deliveries)).
                    forEach(receive(message));
        }
    }

    @SuppressWarnings("unchecked")
    private Consumer<Mailbox> receive(final Object message) {
        return mailbox -> {
            try {
                mailbox.receive(message);
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Exception e) {
                failed.accept(new FailedMessage(this, mailbox, message, e));
            }
        };
    }

    private static Consumer<Mailbox> record(final AtomicInteger deliveries) {
        return subscriber -> deliveries.incrementAndGet();
    }

    private void returnIfDead(final AtomicInteger deliveries,
            final Object message) {
        if (0 == deliveries.get())
            returned.accept(new UnsubscribedMessage(this, message));
    }

    /**
     * Receives messages for a given type and subtypes.
     *
     * @param <T> the message type
     */
    @FunctionalInterface
    public interface Mailbox<T> {
        /**
         * Receives the given <var>message</var>.
         *
         * @param message the received message, never missing
         */
        void receive(@Nonnull final T message)
                throws Exception;
    }

    /** Details on unsubscribed (undelivered) messages. */
    @RequiredArgsConstructor
    public static final class UnsubscribedMessage {
        public final MagicBus bus;
        public final Object message;
    }

    /** Details on failed messages (dead letters). */
    @RequiredArgsConstructor
    public static final class FailedMessage
            extends RuntimeException {
        public final MagicBus bus;
        public final Mailbox mailbox;
        public final Object message;
        public final Exception failure;
    }

    private static final class Subscribers {
        private final ConcurrentNavigableMap<Class, Set<Mailbox>> subscribers
                = new ConcurrentSkipListMap<>(Subscribers::classOrder);

        private static int classOrder(final Class<?> a, final Class<?> b) {
            boolean aFirst = b.isAssignableFrom(a);
            boolean bFirst = a.isAssignableFrom(b);

            if (aFirst && !bFirst)
                return 1;
            else if (bFirst && !aFirst)
                return -1;
            else
                return 0;
        }

        private void subscribe(final Class messageType,
                final Mailbox mailbox) {
            subscribers.computeIfAbsent(messageType, Subscribers::mailbox).
                    add(mailbox);
        }

        private void unsubscribe(final Class messageType,
                final Mailbox mailbox) {
            subscribers.compute(messageType, (__, mailboxes) -> {
                if (notRemoved(mailboxes, mailbox))
                    throw new NoSuchElementException();
                return mailboxes.isEmpty() ? null : mailboxes;
            });
        }

        private static boolean notRemoved(final Set<Mailbox> mailboxes,
                final Mailbox mailbox) {
            return null == mailboxes || !mailboxes.remove(mailbox);
        }

        private Stream<Mailbox> of(final Object message) {
            final Class messageType = message.getClass();
            return subscribers.entrySet().stream().
                    filter(subscribedTo(messageType)).
                    flatMap(toMailboxes());
        }

        private static Set<Mailbox> mailbox(final Class messageType) {
            return new CopyOnWriteArraySet<>();
        }

        @SuppressWarnings("unchecked")
        private static Predicate<Entry<Class, Set<Mailbox>>> subscribedTo(
                final Class messageType) {
            return e -> e.getKey().isAssignableFrom(messageType);
        }

        private static Function<Entry<Class, Set<Mailbox>>, Stream<Mailbox>> toMailboxes() {
            return e -> e.getValue().stream();
        }
    }
}
