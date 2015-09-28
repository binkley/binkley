package hm.binkley;

import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * {@code MagicBus} is an intraprocess message bus.  Subscribers call {@link
 * #subscribe(Class, Mailbox)} to register mailboxes for receiving messages.
 * Senders call {@link #post(Object)} to send messages.
 * <p>
 * Delivery is synchronous.  There is no guaranteed order of delivery.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
@RequiredArgsConstructor
public final class MagicBus {
    private final Subscribers subscribers = new Subscribers();
    /** Receives unsubscribed posts. */
    private final Consumer<DeadLetter> returned;
    /** Receives failed posts. */
    private final Consumer<FailedPost> failed;

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
     * Posts a message.  Subscribers to the type of <var>message</var> and its
     * supertypes recieve the message in their mailboxes.
     * <p>
     * If there is no eligible mailbox, <var>message</var> is sent to the
     * "dead letter" box.
     * <p>
     * If posting fails (a mailbox throws a checked exception),
     * <var>message</var> is sent to the "failed post" box.
     *
     * @param message the message, never missing
     */
    public void post(@Nonnull final Object message) {
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
                failed.accept(new FailedPost(this, mailbox, message, e));
            }
        };
    }

    private static Consumer<Mailbox> record(final AtomicInteger deliveries) {
        return subscriber -> deliveries.incrementAndGet();
    }

    private void returnIfDead(final AtomicInteger deliveries,
            final Object message) {
        if (0 == deliveries.get())
            returned.accept(new DeadLetter(this, message));
    }

    @FunctionalInterface
    public interface Mailbox<T> {
        void receive(final T message)
                throws Exception;
    }

    @RequiredArgsConstructor
    public static final class DeadLetter {
        public final MagicBus bus;
        public final Object message;
    }

    @RequiredArgsConstructor
    public static final class FailedPost
            extends RuntimeException {
        public final MagicBus bus;
        public final Mailbox mailbox;
        public final Object message;
        public final Exception failure;
    }

    private static final class Subscribers {
        private final Map<Class, Set<Mailbox>> subscribers
                = new ConcurrentHashMap<>();

        private void subscribe(final Class messageType,
                final Mailbox mailbox) {
            subscribers.computeIfAbsent(messageType, Subscribers::mailbox).
                    add(mailbox);
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
