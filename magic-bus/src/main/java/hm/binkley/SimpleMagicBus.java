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

import static java.util.Objects.requireNonNull;

/**
 * {@code SimpleMagicBus} is simple, synchronous implementation of {@link
 * MagicBus}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Synchronized, really?  First correct, then fast
 */
@RequiredArgsConstructor
public final class SimpleMagicBus
        implements MagicBus {
    /**
     * Discards messages for {@link #returned} and {@link #failed}, a
     * convenience.
     */
    public static <T> Consumer<T> discard() {
        return m -> {};
    }

    private final Subscribers subscribers = new Subscribers();
    /** Receives returned messages. */
    @Nonnull
    private final Consumer<? super ReturnedMessage> returned;
    /** Receives failed messages. */
    @Nonnull
    private final Consumer<? super FailedMessage> failed;

    @Override
    public <T> void subscribe(@Nonnull final Class<T> messageType,
            @Nonnull final Mailbox<? super T> mailbox) {
        subscribers.subscribe(requireNonNull(messageType, "messageType"),
                requireNonNull(mailbox, "mailbox"));
    }

    @Override
    public <T> void unsubscribe(@Nonnull final Class<T> messageType,
            @Nonnull final Mailbox<? super T> mailbox) {
        subscribers.unsubscribe(requireNonNull(messageType, "messageType"),
                requireNonNull(mailbox, "mailbox"));
    }

    @Override
    public void post(@Nonnull final Object message) {
        try (final Stream<Mailbox> mailboxes = subscribers
                .of(requireNonNull(message, "message"))) {
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
            returned.accept(new ReturnedMessage(this, message));
    }

    private static final class Subscribers {
        private final ConcurrentNavigableMap<Class, Set<Mailbox>> subscribers
                = new ConcurrentSkipListMap<>(Subscribers::classOrder);

        private static int classOrder(final Class<?> a, final Class<?> b) {
            final boolean aFirst = b.isAssignableFrom(a);
            final boolean bFirst = a.isAssignableFrom(b);

            if (aFirst && !bFirst)
                return 1;
            else if (bFirst && !aFirst)
                return -1;
            else
                return 0;
        }

        private synchronized void subscribe(final Class messageType,
                final Mailbox mailbox) {
            subscribers.computeIfAbsent(messageType, Subscribers::mailbox).
                    add(mailbox);
        }

        private synchronized void unsubscribe(final Class messageType,
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

        private synchronized Stream<Mailbox> of(final Object message) {
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
