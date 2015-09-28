package hm.binkley.dynafig;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import static java.util.Arrays.asList;
import static hm.binkley.dynafig.DynafigTesting.Args.primitiveTypeParams;
import static hm.binkley.dynafig.DynafigTesting.Args.refTypeParams;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@code DynafigTesting} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
@RequiredArgsConstructor
@RunWith(Parameterized.class)
public abstract class DynafigTesting<T, R, D extends Tracking & Updating> {
    protected static final String KEY = "bob";

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    protected final Args<T, R> args;

    private D dynafig;

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> parameters() {
        return asList(
                refTypeParams("env key with string values", Tracking::track,
                        "sally", "sally", "bill", "bill"),

                primitiveTypeParams("env key with boolean values",
                        Tracking::trackBool, AtomicBoolean::get, "true", true,
                        "false", false, false),

                primitiveTypeParams("env key with integer values",
                        Tracking::trackInt, AtomicInteger::get, "3", 3, "4",
                        4, 0),

                primitiveTypeParams("env key with long values",
                        Tracking::trackLong, AtomicLong::get, "3", 3L, "4",
                        4L, 0L),

                refTypeParams("env key with reference type values",
                        (d, k, o) -> d.trackAs(k, File::new, o), "sally",
                        new File("sally"), "bill", new File("bill")));
    }

    protected final void dynafig(final D dynafig) {
        this.dynafig = dynafig;
    }

    private D dynafig() {
        return dynafig;
    }

    protected abstract void presetValue(final String value);

    @Test
    public final void shouldNotFindMissingKey() {
        assertThat(track().isPresent(), is(false));
    }

    @Test
    public final void shouldHandleNullValue() {
        presetValue(null);

        assertThat(value(), is(equalTo(args.nullValue)));
    }

    @Test
    public final void shouldHandleNonNullValue() {
        presetValue(args.oldValue);

        assertThat(value(), is(equalTo(args.oldExepcted)));
    }

    @Test
    public final void shouldUpdateExistingKey() {
        presetValue(args.oldValue);

        dynafig().update(KEY, args.newValue);

        assertThat(value(), is(equalTo(args.newExepcted)));
    }

    @Test
    public final void shouldObserveExistingKey() {
        presetValue(args.oldValue);

        final AtomicReference<Object> key = new AtomicReference<>();
        final AtomicReference<Object> value = new AtomicReference<>();
        args.track(dynafig(), (k, v) -> {
            key.set(k);
            value.set(v);
        });
        dynafig().update(KEY, args.newValue);

        assertThat("key", key.get(), is(equalTo(KEY)));
        assertThat("value", value.get(), is(equalTo(args.newExepcted)));
    }

    @Test
    public final void shouldComplainWhenUpdatingMissingKey() {
        thrown.expect(IllegalArgumentException.class);

        dynafig().update(KEY, null);
    }

    protected final T value() {
        return args.value(dynafig());
    }

    private Optional<R> track() {
        return args.track(dynafig());
    }

    @FunctionalInterface
    private interface Tracker<T, R> {
        default Optional<R> track(final Tracking dynafig, final String key) {
            return track(dynafig, key, Tracking.IGNORE);
        }

        Optional<R> track(final Tracking dynafig, final String key,
                final BiConsumer<String, ? super T> onUpdate);
    }

    @FunctionalInterface
    private interface Getter<T, R> {
        T get(final R atomic);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @ToString(of = "description")
    protected static final class Args<T, R> {
        public final String description;
        public final Tracker<T, R> tracker;
        public final Getter<T, R> getter;
        public final String oldValue;
        public final T oldExepcted;
        public final String newValue;
        public final T newExepcted;
        public final T nullValue;

        static <T> Object[] refTypeParams(final String description,
                final Tracker<T, AtomicReference<T>> tracker,
                final String oldValue, final T oldExpected,
                final String newValue, final T newExpected) {
            return new Object[]{
                    new Args<>(description, tracker, AtomicReference::get,
                            oldValue, oldExpected, newValue, newExpected,
                            null)};
        }

        static <T, R> Object[] primitiveTypeParams(final String description,
                final Tracker<T, R> tracker, final Getter<T, R> getter,
                final String oldValue, final T oldExpected,
                final String newValue, final T newExpected,
                final T nullValue) {
            return new Object[]{
                    new Args<>(description, tracker, getter, oldValue,
                            oldExpected, newValue, newExpected, nullValue)};
        }

        private Optional<R> track(final Tracking dynafig) {
            return tracker.track(dynafig, KEY);
        }

        private Optional<R> track(final Tracking dynafig,
                final BiConsumer<String, ? super T> onUpdate) {
            return tracker.track(dynafig, KEY, onUpdate);
        }

        private T value(final Tracking dynafig) {
            return getter.get(track(dynafig).get());
        }
    }
}
