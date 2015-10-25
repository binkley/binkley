package hm.binkley;

import hm.binkley.MagicBus.Mailbox;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.IntStream.range;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@code MagicBusTest} tests {@link SimpleMagicBus}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public final class SimpleMagicBusTest
        extends MagicBusTestBase<SimpleMagicBus> {
    public SimpleMagicBusTest() {
        super(SimpleMagicBus::new);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = NullPointerException.class)
    public void shouldRejectMissingReturnedHandlerInConstructor() {
        new SimpleMagicBus(null, failed::add);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = NullPointerException.class)
    public void shouldRejectMissingFailedHandlerInConstructor() {
        new SimpleMagicBus(returned::add, null);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = NullPointerException.class)
    public void shouldRejectMissingMessageTypeInSubscribe() {
        bus.subscribe(null, message -> {
        });
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = NullPointerException.class)
    public void shouldRejectMissingMailboxInSubscribe() {
        bus.subscribe(RightType.class, null);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = NullPointerException.class)
    public void shouldRejectMissingMessageTypeInUnsubscribe() {
        bus.unsubscribe(null, message -> {
        });
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = NullPointerException.class)
    public void shouldRejectMissingMailboxInUnsubscribe() {
        bus.unsubscribe(RightType.class, null);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = NullPointerException.class)
    public void shouldRejectMissingMessageInPublish() {
        bus.post(null);
    }

    @Test
    public void shouldReceiveCorrectType() {
        final List<RightType> messages = new ArrayList<>(1);
        bus.subscribe(RightType.class, messages::add);

        final RightType message = new RightType();
        bus.post(message);

        assertOn(messages).
                delivered(message).
                noneReturned().
                noneFailed();
    }

    @Test
    public void shouldNotReceiveWrongType() {
        final List<LeftType> messages = new ArrayList<>(1);
        bus.subscribe(LeftType.class, messages::add);

        final RightType message = new RightType();
        bus.post(message);

        assertOn(messages).
                noneDelivered().
                returned(with(message)).
                noneFailed();
    }

    @Test
    public void shouldReceiveSubtypes() {
        final List<BaseType> messages = new ArrayList<>(1);
        bus.subscribe(BaseType.class, messages::add);

        final RightType message = new RightType();
        bus.post(message);

        assertOn(messages).
                delivered(message).
                noneReturned().
                noneFailed();
    }

    @Test
    public void shouldSaveDeadLetters() {
        final LeftType message = new LeftType();
        bus.post(message);

        assertOn(noMailbox()).
                noneDelivered().
                returned(with(message)).
                noneFailed();
    }

    @Test
    public void shouldSaveFailedPosts() {
        final Exception failure = new Exception();
        final Mailbox<LeftType> mailbox = failWith(() -> failure);
        bus.subscribe(LeftType.class, mailbox);

        final LeftType message = new LeftType();
        bus.post(message);

        assertOn(noMailbox()).
                noneDelivered().
                noneReturned().
                failed(with(mailbox, message, failure));
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowFailedPostsForUnchecked() {
        bus.subscribe(LeftType.class, failWith(RuntimeException::new));

        bus.post(new LeftType());
    }

    @Test
    public void shouldReceiveEarlierSubscribersFirst() {
        final AtomicInteger delivery = new AtomicInteger();
        final AtomicInteger second = new AtomicInteger();
        final AtomicInteger first = new AtomicInteger();
        final AtomicInteger fourth = new AtomicInteger();
        final AtomicInteger third = new AtomicInteger();

        bus.subscribe(RightType.class, record(delivery, first));
        bus.subscribe(RightType.class, record(delivery, second));
        bus.subscribe(RightType.class, record(delivery, third));
        bus.subscribe(RightType.class, record(delivery, fourth));

        bus.post(new RightType());

        assertThat(first.get(), is(equalTo(0)));
        assertThat(second.get(), is(equalTo(1)));
        assertThat(third.get(), is(equalTo(2)));
        assertThat(fourth.get(), is(equalTo(3)));

        assertOn(noMailbox()).
                noneReturned().
                noneFailed();
    }

    @Test
    public void shouldReceiveOnParentTypeFirst() {
        final AtomicInteger delivery = new AtomicInteger();
        final AtomicInteger farRight = new AtomicInteger();
        final AtomicInteger right = new AtomicInteger();
        final AtomicInteger base = new AtomicInteger();
        final AtomicInteger object = new AtomicInteger();

        // Register is "random" order to avoid anomalies of implementation
        bus.subscribe(RightType.class, record(delivery, right));
        bus.subscribe(FarRightType.class, record(delivery, farRight));
        bus.subscribe(Object.class, record(delivery, object));
        bus.subscribe(BaseType.class, record(delivery, base));

        bus.post(new FarRightType());

        assertThat(object.get(), is(equalTo(0)));
        assertThat(base.get(), is(equalTo(1)));
        assertThat(right.get(), is(equalTo(2)));
        assertThat(farRight.get(), is(equalTo(3)));

        assertOn(noMailbox()).
                noneReturned().
                noneFailed();
    }

    @Test
    public void shouldUnsubscribeOnlyMailbox() {
        final List<LeftType> messages = new ArrayList<>(0);
        final Mailbox<LeftType> mailbox = messages::add;
        bus.subscribe(LeftType.class, mailbox);
        bus.unsubscribe(LeftType.class, mailbox);

        final LeftType message = new LeftType();
        bus.post(message);

        assertOn(messages).
                noneDelivered().
                returned(with(message)).
                noneFailed();
    }

    @Test
    public void shouldUnsubscribeRightMailbox() {
        final List<RightType> messagesA = new ArrayList<>(1);
        final Mailbox<RightType> mailboxB = failWith(Exception::new);
        bus.subscribe(RightType.class, messagesA::add);
        bus.subscribe(RightType.class, mailboxB);
        bus.unsubscribe(RightType.class, mailboxB);

        final RightType message = new RightType();
        bus.post(message);

        assertOn(messagesA).
                delivered(message).
                noneReturned().
                noneFailed();
    }

    @Test(timeout = 2000L)
    public void shouldUnsubscribeThreadSafely()
            throws InterruptedException {
        final int actors = 100;
        final CountDownLatch latch = new CountDownLatch(actors);

        range(0, actors).parallel().
                forEach(actor -> {
                    // Use "new" to guarantee a unique instance
                    final Mailbox<RightType> mailbox = new Discard();
                    bus.subscribe(RightType.class, mailbox);
                    bus.unsubscribe(RightType.class, mailbox);
                    latch.countDown();
                });

        latch.await(1L, SECONDS);

        final RightType message = new RightType();
        bus.post(message);

        assertOn(noMailbox()).
                returned(with(message)).
                noneFailed();
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldComplainWhenUnsubscribingBadMailbox() {
        final Mailbox<RightType> mailbox = m -> {
        };

        bus.unsubscribe(RightType.class, mailbox);
    }

    private static <T> Mailbox<T> record(final AtomicInteger order,
            final AtomicInteger record) {
        return m -> record.set(order.getAndIncrement());
    }

    private static <T, E extends Exception> Mailbox<T> failWith(
            final Supplier<E> ctor) {
        return message -> {
            throw ctor.get();
        };
    }

    private static class Discard
            implements Mailbox<RightType> {
        @Override
        public void receive(@Nonnull final RightType message) {}
    }

    private abstract static class BaseType {}

    private static final class LeftType
            extends BaseType {}

    private static class RightType
            extends BaseType {}

    private static final class FarRightType
            extends RightType {}
}
