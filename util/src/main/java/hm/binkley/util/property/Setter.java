package hm.binkley.util.property;

import javax.annotation.Nonnull;

/**
 * Represents setting a thing, for example, a map entry value or an array
 * element.
 *
 * @param <T> the value type
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
@FunctionalInterface
public interface Setter<T> {
    /**
     * Sets a value backed by, for example, a map entry or an array element.
     *
     * @param value the value, never {@code null}
     */
    void set(@Nonnull final T value);
}
