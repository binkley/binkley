package hm.binkley.util.function;

import java.util.function.Supplier;

/**
 * {@code ThrowingSupplier} is a <em>throwing</em> look-a=like of {@link Supplier}.  It cannot be a
 * {@code Supplier} as it takes throwing versions of suppliers.  Otherwise it is a faithful
 * reproduction.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
@SuppressWarnings("JavaDoc")
@FunctionalInterface
public interface ThrowingSupplier<T, E extends Exception> {
    /** @see Supplier#get() */
    T get()
            throws E, InterruptedException;

    default <D extends RuntimeException> Supplier<T> asSupplier(final Defer<D> defer) {
        return () -> defer.as(this);
    }
}
