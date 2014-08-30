package hm.binkley.util.function;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * {@code ThrowingBiFunction} is a <em>throwing</em> look-a=like of {@link BiFunction}.  It cannot
 * be a {@code BiFunction} as it takes throwing versions of bi-functions.  Otherwise it is a
 * faithful reproduction.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
@SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
@FunctionalInterface
public interface ThrowingBiFunction<T, U, R, E extends Exception> {
    /** @see BiFunction#apply(Object, Object) */
    R apply(final T t, final U u)
            throws E, InterruptedException;

    /** @see BiFunction#andThen(Function) */
    @Nonnull
    default <V> ThrowingBiFunction<T, U, V, E> andThen(
            @Nonnull final ThrowingFunction<? super R, ? extends V, E> after)
            throws E, InterruptedException {
        return (T t, U u) -> after.apply(apply(t, u));
    }

    default <D extends RuntimeException> BiFunction<T, U, R> asBiFunction(final Defer<D> defer) {
        return (u, v) -> defer.as(() -> apply(u, v));
    }
}
