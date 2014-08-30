package hm.binkley.util.function;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * {@code ThrowingPredicate} is a <em>throwing</em> look-a=like of {@link Predicate}.  It cannot be
 * a {@code Predicate} as it takes throwing versions of predicates.  Otherwise it is a faithful
 * reproduction.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
@SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
@FunctionalInterface
public interface ThrowingPredicate<T, E extends Exception> {
    /** @see Predicate#test(Object) */
    boolean test(final T t)
            throws E, InterruptedException;

    /** @see Predicate#and(Predicate) */
    @Nonnull
    default ThrowingPredicate<T, E> and(@Nonnull final ThrowingPredicate<? super T, E> other) {
        return t -> test(t) && other.test(t);
    }

    /** @see Predicate#negate() */
    @Nonnull
    default ThrowingPredicate<T, E> negate() {
        return t -> !test(t);
    }

    /** @see Predicate#or(Predicate) */
    @Nonnull
    default ThrowingPredicate<T, E> or(@Nonnull final ThrowingPredicate<? super T, E> other) {
        return t -> test(t) || other.test(t);
    }

    /** @see Predicate#isEqual(Object) */
    @Nonnull
    static <T, E extends Exception> ThrowingPredicate<T, RuntimeException> isEqual(
            final Object targetRef) {
        return null == targetRef ? Objects::isNull : targetRef::equals;
    }

    default <D extends RuntimeException> Predicate<T> asPredicate(
            final Defer<D> defer) {
        return t -> defer.as(() -> test(t));
    }
}
