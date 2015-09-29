package hm.binkley;

import hm.binkley.MagicBus.FailedMessage;
import hm.binkley.MagicBus.Mailbox;
import hm.binkley.MagicBus.UnsubscribedMessage;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * {@code MagicBusTest} tests {@link MagicBus}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public final class MagicBusTest {
    private MagicBus bus;
    private AtomicReference<UnsubscribedMessage> returned;
    private AtomicReference<FailedMessage> failed;

    @Before
    public void setUpFixture() {
        returned = new AtomicReference<>();
        failed = new AtomicReference<>();
        bus = new MagicBus(returned::set, failed::set);
    }

    @Test
    public void shouldReceiveCorrectType() {
        final AtomicReference<RightType> mailbox = new AtomicReference<>();
        bus.subscribe(RightType.class, mailbox::set);

        bus.publish(new RightType());

        assertThat(mailbox.get(), is(notNullValue()));
    }

    @Test
    public void shouldNotReceiveWrongType() {
        final AtomicReference<LeftType> mailbox = new AtomicReference<>();
        bus.subscribe(LeftType.class, mailbox::set);

        bus.publish(new RightType());

        assertThat(mailbox.get(), is(nullValue()));
    }

    @Test
    public void shouldReceiveSubtypes() {
        final AtomicReference<BaseType> mailbox = new AtomicReference<>();
        bus.subscribe(BaseType.class, mailbox::set);

        bus.publish(new RightType());

        assertThat(mailbox.get(), is(notNullValue()));
    }

    @Test
    public void shouldSaveDeadLetters() {
        bus.publish(new LeftType());

        assertThat(returned.get(), is(notNullValue()));
    }

    @Test
    public void shouldSaveFailedPosts() {
        bus.subscribe(LeftType.class, failWith(Exception::new));

        bus.publish(new LeftType());

        assertThat(failed.get(), is(notNullValue()));
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowFailedPostsForUnchecked() {
        bus.subscribe(LeftType.class, failWith(RuntimeException::new));

        bus.publish(new LeftType());
    }

    @Test
    public void shouldReceiveEarlierSubscribersFirst() {
        final AtomicInteger order = new AtomicInteger();
        final AtomicInteger second = new AtomicInteger();
        final AtomicInteger first = new AtomicInteger();
        final AtomicInteger fourth = new AtomicInteger();
        final AtomicInteger third = new AtomicInteger();
        // Register is "random" order to avoid anomalies of implementation
        bus.subscribe(RightType.class, record(order, first));
        bus.subscribe(RightType.class, record(order, second));
        bus.subscribe(RightType.class, record(order, third));
        bus.subscribe(RightType.class, record(order, fourth));

        bus.publish(new RightType());

        assertThat(first.get(), is(equalTo(0)));
        assertThat(second.get(), is(equalTo(1)));
        assertThat(third.get(), is(equalTo(2)));
        assertThat(fourth.get(), is(equalTo(3)));
    }

    @Test
    public void shouldReceiveOnParentTypeFirst() {
        final AtomicInteger order = new AtomicInteger();
        final AtomicInteger farRight = new AtomicInteger();
        final AtomicInteger right = new AtomicInteger();
        final AtomicInteger base = new AtomicInteger();
        final AtomicInteger object = new AtomicInteger();
        // Register is "random" order to avoid anomalies of implementation
        bus.subscribe(RightType.class, record(order, right));
        bus.subscribe(FarRightType.class, record(order, farRight));
        bus.subscribe(Object.class, record(order, object));
        bus.subscribe(BaseType.class, record(order, base));

        bus.publish(new FarRightType());

        assertThat(object.get(), is(equalTo(0)));
        assertThat(base.get(), is(equalTo(1)));
        assertThat(right.get(), is(equalTo(2)));
        assertThat(farRight.get(), is(equalTo(3)));
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
