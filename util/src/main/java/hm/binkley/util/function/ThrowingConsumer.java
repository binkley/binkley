package hm.binkley.util.function;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * {@code ThrowingConsumer} is a <em>throwing</em> look-a=like of {@link
 * Consumer}.  It cannot be a {@code Consumer} as it takes throwing versions
 * of consumers.  Otherwise it is a faithful reproduction.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
@SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
@FunctionalInterface
public interface ThrowingConsumer<T, E extends Exception> {
    /** @see Consumer#accept(Object) */
    void accept(final T t)
            throws E, InterruptedException;

    /** @see Consumer#andThen(Consumer) */
    @Nonnull
    default ThrowingConsumer<T, E> andThen(
            @Nonnull final ThrowingConsumer<? super T, E> after) {
        return (T t) -> {
            accept(t);
            after.accept(t);
        };
    }

    /** Creates a facade {@code Consumer} wrapping this throwing one. */
    default <D extends RuntimeException> Consumer<T> asConsumer(
            final DeferredFunction<D> defer) {
        return t -> defer.as(() -> accept(t));
    }
}
