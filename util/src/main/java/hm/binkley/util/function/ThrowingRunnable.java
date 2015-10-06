package hm.binkley.util.function;

/**
 * {@code ThrowingRunnable} is a <em>throwing</em> look-a=like of {@link Runnable}.  It cannot be a
 * {@code Runnable} as it throws.  Otherwise it is a faithful reproduction.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
@SuppressWarnings("JavaDoc")
@FunctionalInterface
public interface ThrowingRunnable<E extends Exception> {
    /** @see Runnable#run() */
    void run()
            throws E, InterruptedException;

    /** Creates a facade {@code Runnable} wrapping this throwing one. */
    default <D extends RuntimeException> Runnable asRunnable(final DeferredFunction<D> defer) {
        return () -> defer.as(this);
    }
}
