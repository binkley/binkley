package hm.binkley.util.function;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.function.BinaryOperator;

/**
 * {@code ThrowingBinaryOperator} is a <em>throwing</em> look-a=like of {@link BinaryOperator}.  It
 * cannot be a {@code BinaryOperator} as it takes throwing versions of binary operators.  Otherwise
 * it is a faithful reproduction.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
@SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
@FunctionalInterface
public interface ThrowingBinaryOperator<T, E extends Exception>
        extends ThrowingBiFunction<T, T, T, E> {
    /** @see BinaryOperator#minBy(Comparator) */
    @Nonnull
    static <T, E extends Exception> ThrowingBinaryOperator<T, E> minBy(
            @Nonnull final Comparator<? super T> comparator) {
        return (a, b) -> 0 >= comparator.compare(a, b) ? a : b;
    }

    /** @see BinaryOperator#maxBy(Comparator) */
    @Nonnull
    static <T, E extends Exception> ThrowingBinaryOperator<T, E> maxBy(
            @Nonnull final Comparator<? super T> comparator) {
        return (a, b) -> 0 <= comparator.compare(a, b) ? a : b;
    }

    /** Creates a facade {@code BinaryOperator} wrapping this throwing one. */
    default <D extends RuntimeException> BinaryOperator<T> asBinaryOperator(final Defer<D> defer) {
        return (u, v) -> defer.as(() -> apply(u, v));
    }
}
