package hm.binkley.util.property;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Represents access to a thing, for example, a map entry value or an array
 * element.
 *
 * @param <T> the return type
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
@FunctionalInterface
public interface Getter<T>
        extends Iterable<T>, Supplier<T> {
    @Nonnull
    default <R> R collect(@Nonnull final Supplier<R> supplier,
            @Nonnull final BiConsumer<R, ? super T> accumulator) {
        final R c = supplier.get();
        accumulator.accept(c, get());
        return c;
    }

    @Nonnull
    default <R, A> R collect(
            @Nonnull final Collector<? super T, A, R> collector) {
        final A c = collector.supplier().get();
        collector.accumulator().accept(c, get());
        return collector.finisher().apply(c);
    }

    @Nonnull
    default Optional<T> collect() {
        return Optional.ofNullable(get());
    }

    @Override
    default Iterator<T> iterator() {
        return new Iterator<T>() {
            private boolean hasNext = true;

            @Override
            public boolean hasNext() {
                return hasNext;
            }

            @Override
            public T next() {
                if (!hasNext)
                    throw new NoSuchElementException();
                hasNext = false;
                return get();
            }
        };
    }

    @Override
    default void forEach(@Nonnull Consumer<? super T> action) {
        action.accept(get());
    }

    /**
     * Returns a value backed by, for example, a map entry or an array
     * element.
     *
     * @return the value, never {@code null}
     */
    @Nullable
    @Override
    T get();
}
