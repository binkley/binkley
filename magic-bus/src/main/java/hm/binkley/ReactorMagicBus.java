package hm.binkley;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.bus.registry.Registration;
import reactor.bus.spec.EventBusSpec;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static reactor.bus.Event.wrap;
import static reactor.bus.selector.Selectors.type;

/**
 * {@code ReactorMagicBus} is an implementation of {@link MagicBus} based on
 * <a href="http://projectreactor.io/">Reactor</a>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Caller-supplied event bus
 * @todo Asynchronous delivery - testing needs revisiting
 */
@RequiredArgsConstructor(staticName = "of")
public final class ReactorMagicBus
        implements MagicBus {
    private final EventBus bus = new EventBusSpec().
            synchronousDispatcher().
            uncaughtErrorHandler(ReactorMagicBus::rethrowUnchecked).
            get();
    private final Map<Subscription<?>, Registration<Object, reactor.fn.Consumer<? extends Event<?>>>>
            subscriptions = new ConcurrentHashMap<>();

    /** Receives returned messages. */
    @Nonnull
    private final Consumer<? super ReturnedMessage> returned;
    /** Receives failed messages. */
    @Nonnull
    private final Consumer<? super FailedMessage> failed;

    public <T> void subscribe(@Nonnull final Class<T> type,
            @Nonnull final Mailbox<? super T> mailbox) {
        subscriptions.put(Subscription
                        .of(requireNonNull(type), requireNonNull(mailbox)),
                bus.on(type(type), event -> receive(mailbox, event)));
    }

    public <T> void unsubscribe(@Nonnull final Class<T> type,
            @Nonnull final Mailbox<? super T> mailbox) {
        ofNullable(subscriptions.remove(Subscription
                .of(requireNonNull(type), requireNonNull(mailbox)))).
                orElseThrow(NoSuchElementException::new).
                cancel();
    }

    public void post(@Nonnull final Object message) {
        final Class<?> type = message.getClass();
        if (!bus.respondsToKey(type)) {
            returned.accept(new ReturnedMessage(this, message));
            return;
        }

        bus.notify(type, wrap(message));
    }

    @SuppressWarnings("unchecked")
    private <T> void receive(final Mailbox<? super T> mailbox,
            final Event<?> event) {
        final T message = (T) event.getData();
        try {
            mailbox.receive(message);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            failed.accept(new FailedMessage(this, mailbox, message, e));
        }
    }

    private static void rethrowUnchecked(final Throwable t) {
        if (t instanceof RuntimeException)
            throw (RuntimeException) t;
        else if (t instanceof Error)
            throw (Error) t;
        else
            throw new RuntimeException("BUG: Did not handle " + t);
    }

    @EqualsAndHashCode
    @RequiredArgsConstructor(staticName = "of")
    private static final class Subscription<T> {
        private final Class<T> type;
        private final Mailbox<? super T> mailbox;
    }
}
