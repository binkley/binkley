package hm.binkley.util.function;

import javax.annotation.Nonnull;
import java.util.function.Function;

/**
 * {@code ThrowingFunction} is a <em>throwing</em> look-a=like of {@link Function}.  It cannot be a
 * {@code Function} as it takes throwing versions of functions.  Otherwise it is a faithful
 * reproduction.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
@SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Exception> {
    /** @see Function#apply(Object) */
    R apply(final T t)
            throws E, InterruptedException;

    /** @see Function#compose(Function) */
    @Nonnull
    default <V> ThrowingFunction<V, R, E> compose(
            @Nonnull final ThrowingFunction<? super V, ? extends T, E> before) {
        return (V v) -> apply(before.apply(v));
    }

    /** @see Function#andThen(Function) */
    @Nonnull
    default <V> ThrowingFunction<T, V, E> andThen(
            @Nonnull final ThrowingFunction<? super R, ? extends V, E> after) {
        return (T t) -> after.apply(apply(t));
    }

    /** @see Function#identity() */
    @Nonnull
    static <T> ThrowingFunction<T, T, RuntimeException> identity() {
        return t -> t;
    }

    default <D extends RuntimeException> Function<T, R> asFunction(
            final Defer<D> defer) {
        return t -> defer.as(() -> apply(t));
    }
}
