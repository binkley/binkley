package hm.binkley;

import hm.binkley.MagicBus.FailedMessage;
import hm.binkley.MagicBus.Mailbox;
import hm.binkley.MagicBus.ReturnedMessage;
import lombok.RequiredArgsConstructor;
import org.junit.Before;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@code MagicBusTestBase} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
@RequiredArgsConstructor
public abstract class MagicBusTestBase<B extends MagicBus> {
    private final BiFunction<Consumer<? super ReturnedMessage>, Consumer<? super FailedMessage>, B>
            ctor;

    protected List<ReturnedMessage> returned;
    protected List<FailedMessage> failed;
    protected B bus;

    @Before
    public void setUpFixtures() {
        returned = new CopyOnWriteArrayList<>();
        failed = new CopyOnWriteArrayList<>();
        bus = ctor.apply(returned::add, failed::add);
    }

    protected final <T> AssertDelivery<T> assertOn(final List<T> delivered) {
        return new AssertDelivery<>(delivered);
    }

    protected static <T> List<T> noMailbox() {
        return emptyList();
    }

    protected final ReturnedMessage with(final Object message) {
        return new ReturnedMessage(bus, message);
    }

    protected final FailedMessage with(final Mailbox mailbox,
            final Object message, final Exception failure) {
        return new FailedMessage(bus, mailbox, message, failure);
    }

    @RequiredArgsConstructor(access = PRIVATE)
    protected final class AssertDelivery<T> {
        private final List<T> delivered;

        public AssertDelivery<T> noneDelivered() {
            assertThat(delivered, is(empty()));
            return this;
        }

        @SafeVarargs
        public final <U extends T> AssertDelivery<T> delivered(
                final U... delivered) {
            assertThat(this.delivered, is(asList(delivered)));
            return this;
        }

        public AssertDelivery<T> noneReturned() {
            assertThat(returned, is(empty()));
            return this;
        }

        public AssertDelivery<T> returned(final ReturnedMessage... returned) {
            assertThat(MagicBusTestBase.this.returned,
                    is(equalTo(asList(returned))));
            return this;
        }

        public AssertDelivery<T> noneFailed() {
            assertThat(failed, is(empty()));
            return this;
        }

        public AssertDelivery<T> failed(final FailedMessage... failed) {
            assertThat(MagicBusTestBase.this.failed,
                    is(equalTo(asList(failed))));
            return this;
        }
    }
}
