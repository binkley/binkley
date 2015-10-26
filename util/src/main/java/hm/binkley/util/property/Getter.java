package hm.binkley.util.property;

import javax.annotation.Nonnull;

/**
 * Represents access to a thing, for example, a map entry value or an array
 * element.
 *
 * @param <T> the return type
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
@FunctionalInterface
public interface Getter<T> {
    /**
     * Returns a value backed by, for example, a map entry or an array
     * element.
     *
     * @return the value, never {@code null}
     */
    @Nonnull
    T get();
}
