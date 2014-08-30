package hm.binkley.util.function;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;

/**
 * {@code ThrowingBiConsumer} is a <em>throwing</em> look-a=like of {@link BiConsumer}.  It cannot
 * be a {@code BiConsumer} as it takes throwing versions of bi-consumers.  Otherwise it is a
 * faithful reproduction.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
@SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
@FunctionalInterface
public interface ThrowingBiConsumer<T, U, E extends Exception> {
    /** @see BiConsumer#accept(Object, Object) */
    void accept(final T t, final U u)
            throws E, InterruptedException;

    /** @see BiConsumer#andThen(BiConsumer) */
    @Nonnull
    default ThrowingBiConsumer<T, U, E> andThen(
            @Nonnull final ThrowingBiConsumer<? super T, ? super U, E> after) {
        return (l, r) -> {
            accept(l, r);
            after.accept(l, r);
        };
    }

    /** Creates a facade {@code BiConsumer} wrapping this throwing one. */
    default <D extends RuntimeException> BiConsumer<T, U> asBiConsumer(final Defer<D> defer) {
        return (t, u) -> defer.as(() -> accept(t, u));
    }
}
