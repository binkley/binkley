package hm.binkley;

import hm.binkley.MagicBus.FailedMessage;
import hm.binkley.MagicBus.Mailbox;
import hm.binkley.MagicBus.UnsubscribedMessage;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static hm.binkley.MagicBus.discard;
import static org.hamcrest.Matchers.equalTo;
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
        returned = new ArrayList<>();
        failed = new ArrayList<>();
        bus = new MagicBus(returned::add, failed::add);
    }

    @Test
    public void shouldReceiveCorrectType() {
        final List<RightType> mailbox = new ArrayList<>();
        bus.subscribe(RightType.class, mailbox::add);

        bus.publish(new RightType());

        assertThat(mailbox.isEmpty(), is(false));
    }

    @Test
    public void shouldNotReceiveWrongType() {
        final List<LeftType> mailbox = new ArrayList<>();
        bus.subscribe(LeftType.class, mailbox::add);

        bus.publish(new RightType());

        assertThat(mailbox.isEmpty(), is(true));
    }

    @Test
    public void shouldReceiveSubtypes() {
        final List<BaseType> mailbox = new ArrayList<>();
        bus.subscribe(BaseType.class, mailbox::add);

        bus.publish(new RightType());

        assertThat(mailbox.isEmpty(), is(false));
    }

    @Test
    public void shouldSaveDeadLetters() {
        bus.publish(new LeftType());

        assertThat(returned.isEmpty(), is(false));
    }

    @Test
    public void shouldSaveFailedPosts() {
        bus.subscribe(LeftType.class, failWith(Exception::new));

        bus.publish(new LeftType());

        assertThat(failed.isEmpty(), is(false));
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
    }

    @Test
    public void shouldDiscardDeadLetters() {
        bus = new MagicBus(discard(), discard());

        bus.publish(new RightType());
    }

    @Test
    public void shouldDiscardFailedPosts() {
        bus = new MagicBus(discard(), discard());
        bus.subscribe(RightType.class, m -> {
            throw new Exception();
        });

        bus.publish(new RightType());
    }

    @Test
    public void shouldUnsubscribe() {
        final Mailbox<RightType> mailbox = m -> {
            throw new Exception();
        };
        bus.subscribe(RightType.class, mailbox);
        bus.unsubscribe(RightType.class, mailbox);

        bus.publish(new RightType());

        assertThat(returned.isEmpty(), is(false));
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldComplainWhenUnsubscribingNonSubscribedMailbox() {
        final Mailbox<RightType> mailbox = m -> {};

        bus.unsubscribe(RightType.class, mailbox);
    }

    private static <T> Mailbox<T> record(final AtomicInteger order,
            final AtomicInteger record) {
        return m -> record.set(order.getAndIncrement());
    }

    private static <T, U extends Exception> Mailbox<T> failWith(
            final Supplier<U> ctor) {
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
