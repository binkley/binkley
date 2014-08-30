package hm.binkley.util.function;

import java.util.function.LongSupplier;

/**
 * {@code ThrowingLongSupplier} is a <em>throwing</em> look-a=like of {@link LongSupplier}. It
 * cannot be a {@code LongSupplier} as it takes throwing versions of long suppliers. Otherwise it is
 * a faithful reproduction.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
@SuppressWarnings("JavaDoc")
@FunctionalInterface
public interface ThrowingLongSupplier<E extends Exception> {
    /** @see LongSupplier#getAsLong() */
    long getAsLong()
            throws E, InterruptedException;

    default <D extends RuntimeException> LongSupplier asLongSupplier(final Defer<D> defer) {
        return () -> defer.as(this);
    }
}
