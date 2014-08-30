package hm.binkley.util.function;

import java.util.function.BooleanSupplier;

/**
 * {@code ThrowingBooleanSupplier} is a <em>throwing</em> look-a=like of {@link BooleanSupplier}. It
 * cannot be a {@code BooleanSupplier} as it takes throwing versions of boolean suppliers. Otherwise
 * it is a faithful reproduction.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
@SuppressWarnings("JavaDoc")
@FunctionalInterface
public interface ThrowingBooleanSupplier<E extends Exception> {
    /** @see BooleanSupplier#getAsBoolean() */
    boolean getAsBoolean()
            throws E, InterruptedException;

    default <D extends RuntimeException> BooleanSupplier asBooleanSupplier(final Defer<D> defer) {
        return () -> defer.as(this);
    }
}
