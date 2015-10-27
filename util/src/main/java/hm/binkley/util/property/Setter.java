package hm.binkley.util.property;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Represents setting a thing, for example, a map entry value or an array
 * element.
 *
 * @param <T> the value type
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
@FunctionalInterface
public interface Setter<T>
        extends Consumer<T> {
    @Override
    default void accept(@Nullable final T value) {
        set(value);
    }

    /**
     * Sets a value backed by, for example, a map entry or an array element.
     *
     * @param value the value, never {@code null}
     */
    void set(@Nullable final T value);
}
