package lab.dynafig;

import lombok.ToString;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

/**
 * {@code DefaultDynafig} is a simple implementation of {@link Tracking}.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">B. K. Oxley</a>
 */
public class DefaultDynafig
        implements Tracking, Updating {
    private final Map<String, Value> values = new ConcurrentHashMap<>();

    @FunctionalInterface
    public interface Fetcher
            extends Function<String, Optional<String>> {}

    private static final Fetcher none = k -> null;
    private final Fetcher fetcher;

    public DefaultDynafig() {
        this(none);
    }

    public DefaultDynafig(final Fetcher fetcher) {
        this.fetcher = fetcher;
    }

    public DefaultDynafig(@Nonnull final Map<String, String> pairs) {
        this(pairs, none);
    }

    public DefaultDynafig(@Nonnull final Map<String, String> pairs,
            final Fetcher fetcher) {
        this(fetcher);
        pairs.forEach((k, v) -> values.put(k, new Value(v)));
    }

    public DefaultDynafig(
            @Nonnull final Stream<Entry<String, String>> pairs) {
        this(pairs, none);
    }

    public DefaultDynafig(@Nonnull final Stream<Entry<String, String>> pairs,
            final Fetcher fetcher) {
        this(fetcher);
        pairs.forEach(pair -> values.
                put(pair.getKey(), new Value(pair.getValue())));
    }

    @SuppressWarnings("unchecked")
    public DefaultDynafig(@Nonnull final Properties properties) {
        this(properties, none);
    }

    @SuppressWarnings("unchecked")
    public DefaultDynafig(@Nonnull final Properties properties,
            final Fetcher fetcher) {
        this((Map) properties, fetcher);
    }

    @Nonnull
    @Override
    public Optional<AtomicReference<String>> track(@Nonnull final String key,
            @Nonnull final BiConsumer<String, ? super String> onUpdate) {
        return track(key, Value::track, onUpdate);
    }

    @Nonnull
    @Override
    public Optional<AtomicBoolean> trackBool(@Nonnull final String key,
            @Nonnull final BiConsumer<String, ? super Boolean> onUpdate) {
        return track(key, Value::trackBool, onUpdate);
    }

    @Nonnull
    @Override
    public Optional<AtomicInteger> trackInt(@Nonnull final String key,
            @Nonnull final BiConsumer<String, ? super Integer> onUpdate) {
        return track(key, Value::trackInt, onUpdate);
    }

    @Nonnull
    @Override
    public Optional<AtomicLong> trackLong(@Nonnull final String key,
            @Nonnull final BiConsumer<String, ? super Long> onUpdate) {
        return track(key, Value::trackLong, onUpdate);
    }

    @Nonnull
    @Override
    public <T> Optional<AtomicReference<T>> trackAs(@Nonnull final String key,
            @Nonnull final Function<String, T> convert,
            @Nonnull final BiConsumer<String, ? super T> onUpdate) {
        return track(key, (v, c) -> v.trackAs(convert, c), onUpdate);
    }

    @Override
    public void update(@Nonnull final String key, final String value) {
        values.compute(key, (k, v) -> {
            if (null == v)
                throw new IllegalArgumentException(key);
            return v.update(value);
        });
    }

    private <T, R> Optional<R> track(final String key,
            final BiFunction<Value, Consumer<T>, R> tracker,
            final BiConsumer<String, ? super T> onUpdate) {
        return value(key).
                map(v -> tracker.apply(v, curry(key, onUpdate)));
    }

    private Optional<Value> value(final String key) {
        return ofNullable(values.computeIfAbsent(key, k -> fetch(key)));
    }

    private Value fetch(final String key) {
        final Optional<String> fetched = fetcher.apply(key);
        if (null == fetched)
            return null;
        return new Value(fetched.orElse(null));
    }

    private static <U> Consumer<U> curry(final String key,
            final BiConsumer<String, ? super U> onUpdate) {
        return u -> onUpdate.accept(key, u);
    }

    @ToString
    private static final class Value {
        private final String value;
        private final List<Atomic<?, ?>> atomics;

        private Value(final String value) {
            this(value, new CopyOnWriteArrayList<>());
        }

        private Value(final String value, final List<Atomic<?, ?>> atomics) {
            this.value = value;
            this.atomics = atomics;
            atomics.stream().
                    forEach(a -> a.accept(value));
        }

        private Value update(final String value) {
            return Objects.equals(this.value, value) ? this
                    : new Value(value, atomics);
        }

        private AtomicReference<String> track(
                final Consumer<? super String> onUpdate) {
            final Atomic<String, AtomicReference<String>> atomic
                    = new Atomic<>(value, new AtomicReference<>(),
                    AtomicReference::get, AtomicReference::set, onUpdate);
            atomics.add(atomic);
            return atomic.atomic;
        }

        private AtomicBoolean trackBool(
                final Consumer<? super Boolean> onUpdate) {
            final Atomic<Boolean, AtomicBoolean> atomic = new Atomic<>(value,
                    new AtomicBoolean(), AtomicBoolean::get,
                    (a, v) -> a.set(null == v ? false : Boolean.valueOf(v)),
                    onUpdate);
            atomics.add(atomic);
            return atomic.atomic;
        }

        private AtomicInteger trackInt(
                final Consumer<? super Integer> onUpdate) {
            final Atomic<Integer, AtomicInteger> atomic = new Atomic<>(value,
                    new AtomicInteger(), AtomicInteger::get,
                    (a, v) -> a.set(null == v ? 0 : Integer.valueOf(v)),
                    onUpdate);
            atomics.add(atomic);
            return atomic.atomic;
        }

        private AtomicLong trackLong(
                final Consumer<? super Long> onUpdate) {
            final Atomic<Long, AtomicLong> atomic = new Atomic<>(value,
                    new AtomicLong(), AtomicLong::get,
                    (a, v) -> a.set(null == v ? 0L : Long.valueOf(v)),
                    onUpdate);
            atomics.add(atomic);
            return atomic.atomic;
        }

        private <T> AtomicReference<T> trackAs(
                final Function<? super String, T> convert,
                final Consumer<? super T> onUpdate) {
            final Atomic<T, AtomicReference<T>> atomic = new Atomic<>(value,
                    new AtomicReference<>(), AtomicReference::get,
                    (a, v) -> a.set(null == v ? null : convert.apply(v)),
                    onUpdate);
            atomics.add(atomic);
            return atomic.atomic;
        }
    }

    private static final class Atomic<T, R>
            implements Consumer<String>, Supplier<T> {
        private final R atomic;
        private final Function<R, T> getter;
        private final BiConsumer<R, String> setter;
        private final Consumer<? super T> onUpdate;

        private Atomic(final String value, final R atomic,
                final Function<R, T> getter,
                final BiConsumer<R, String> setter,
                final Consumer<? super T> onUpdate) {
            this.atomic = atomic;
            this.getter = getter;
            this.setter = setter;
            this.onUpdate = onUpdate;
            accept(value);
        }

        @Override
        public final T get() {
            return getter.apply(atomic);
        }

        @Override
        public final void accept(final String value) {
            setter.accept(atomic, value);
            onUpdate.accept(get());
        }
    }
}
