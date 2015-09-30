package hm.binkley;

import hm.binkley.MagicBus.FailedMessage;
import hm.binkley.MagicBus.Mailbox;
import hm.binkley.MagicBus.UnsubscribedMessage;
import lombok.RequiredArgsConstructor;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static hm.binkley.MagicBus.discard;
import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@code MagicBusTest} tests {@link MagicBus}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public final class MagicBusTest {
    private MagicBus bus;
    private List<UnsubscribedMessage> returned;
    private List<FailedMessage> failed;

    @Before
    public void setUpFixture() {
        returned = new ArrayList<>(1);
        failed = new ArrayList<>(1);
        bus = new MagicBus(returned::add, failed::add);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = NullPointerException.class)
    public void shouldRejectMissingReturnedHandlerInConstructor() {
        new MagicBus(null, failed::add);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = NullPointerException.class)
    public void shouldRejectMissingFailedHandlerInConstructor() {
        new MagicBus(returned::add, null);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = NullPointerException.class)
    public void shouldRejectMissingMessageTypeInSubscribe() {
        bus.subscribe(null, message -> {});
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = NullPointerException.class)
    public void shouldRejectMissingMailboxInSubscribe() {
        bus.subscribe(RightType.class, null);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = NullPointerException.class)
    public void shouldRejectMissingMessageTypeInUnsubscribe() {
        bus.unsubscribe(null, message -> {});
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = NullPointerException.class)
    public void shouldRejectMissingMailboxInUnsubscribe() {
        bus.unsubscribe(RightType.class, null);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = NullPointerException.class)
    public void shouldRejectMissingMessageInPublish() {
        bus.publish(null);
    }

    @Test
    public void shouldReceiveCorrectType() {
        final List<RightType> messages = new ArrayList<>(1);
        bus.subscribe(RightType.class, messages::add);

        bus.publish(new RightType());

        assertOn(messages).
                delivered(1).
                returned(0).
                failed(0);
    }

    @Test
    public void shouldNotReceiveWrongType() {
        final List<LeftType> messages = new ArrayList<>(1);
        bus.subscribe(LeftType.class, messages::add);

        bus.publish(new RightType());

        assertOn(messages).
                delivered(0).
                returned(1).
                failed(0);
    }

    @Test
    public void shouldReceiveSubtypes() {
        final List<BaseType> messages = new ArrayList<>(1);
        bus.subscribe(BaseType.class, messages::add);

        bus.publish(new RightType());

        assertOn(messages).
                delivered(1).
                returned(0).
                failed(0);
    }

    @Test
    public void shouldSaveDeadLetters() {
        final List<BaseType> messages = new ArrayList<>(0);
        bus.publish(new LeftType());

        assertOn(messages).
                delivered(0).
                returned(1).
                failed(0);
    }

    @Test
    public void shouldSaveFailedPosts() {
        final List<BaseType> messages = new ArrayList<>(0);
        bus.subscribe(LeftType.class, failWith(Exception::new));

        bus.publish(new LeftType());

        assertOn(messages).
                delivered(0).
                returned(0).
                failed(1);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowFailedPostsForUnchecked() {
        bus.subscribe(LeftType.class, failWith(RuntimeException::new));

        bus.publish(new LeftType());
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

        bus.publish(new RightType());

        assertThat(first.get(), is(equalTo(0)));
        assertThat(second.get(), is(equalTo(1)));
        assertThat(third.get(), is(equalTo(2)));
        assertThat(fourth.get(), is(equalTo(3)));

        assertOn(null).
                returned(0).
                failed(0);
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

        bus.publish(new FarRightType());

        assertThat(object.get(), is(equalTo(0)));
        assertThat(base.get(), is(equalTo(1)));
        assertThat(right.get(), is(equalTo(2)));
        assertThat(farRight.get(), is(equalTo(3)));

        assertOn(null).
                returned(0).
                failed(0);
    }

    @Test
    public void shouldDiscardDeadLetters() {
        bus = new MagicBus(discard(), discard());

        bus.publish(new RightType());

        assertOn(null).
                returned(0).
                failed(0);
    }

    @Test
    public void shouldDiscardFailedPosts() {
        bus = new MagicBus(discard(), discard());
        bus.subscribe(RightType.class, failWith(Exception::new));

        bus.publish(new RightType());

        assertOn(null).
                returned(0).
                failed(0);
    }

    @Test
    public void shouldUnsubscribeOnlyMailbox() {
        final List<LeftType> messages = new ArrayList<>(0);
        final Mailbox<LeftType> mailbox = messages::add;
        bus.subscribe(LeftType.class, mailbox);
        bus.unsubscribe(LeftType.class, mailbox);

        bus.publish(new LeftType());

        assertOn(messages).
                delivered(0).
                returned(1).
                failed(0);
    }

    @Test
    public void shouldUnsubscribeRightMailbox() {
        final List<RightType> messagesA = new ArrayList<>(1);
        final Mailbox<RightType> mailboxB = failWith(Exception::new);
        bus.subscribe(RightType.class, messagesA::add);
        bus.subscribe(RightType.class, mailboxB);
        bus.unsubscribe(RightType.class, mailboxB);

        bus.publish(new RightType());

        assertOn(messagesA).
                delivered(1).
                returned(0).
                failed(0);
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldComplainWhenUnsubscribingBadMailbox() {
        final Mailbox<RightType> mailbox = m -> {};

        bus.unsubscribe(RightType.class, mailbox);
    }

    private AssertDelivery assertOn(final List messages) {
        return new AssertDelivery(messages);
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private final class AssertDelivery {
        private final List<?> messages;

        private AssertDelivery delivered(final int delivered) {
            assertThat(messages, hasSize(delivered));
            return this;
        }

        private AssertDelivery returned(final int returned) {
            assertThat(MagicBusTest.this.returned, hasSize(returned));
            return this;
        }

        private AssertDelivery failed(final int failed) {
            assertThat(MagicBusTest.this.failed, hasSize(failed));
            return this;
        }
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

    private abstract static class BaseType {}

    private static final class LeftType
            extends BaseType {}

    private static class RightType
            extends BaseType {}

    private static final class FarRightType
            extends RightType {}
}
