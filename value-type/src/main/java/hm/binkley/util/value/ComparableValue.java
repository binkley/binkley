package hm.binkley.util.value;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

/**
 * {@code StringValue} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public abstract class ComparableValue<T extends Comparable<T>, V extends ComparableValue<T, V>>
        extends Value<T, V>
        implements Comparable<V> {
    protected ComparableValue(@Nonnull final T value) {
        super(value);
    }

    @Override
    public final int compareTo(@Nonnull final V that) {
        requireNonNull(that, "that");
        return value.compareTo(that.value);
    }
}
