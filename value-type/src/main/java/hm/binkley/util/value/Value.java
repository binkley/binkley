package hm.binkley.util.value;

import javax.annotation.Nonnull;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * {@code Value} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
//@EqualsAndHashCode
public abstract class Value<T, V extends Value<T, V>>
        implements Cloneable {
    @Nonnull
    protected final T value;

    protected Value(@Nonnull final T value) {
        this.value = value;
    }

    @Nonnull
    public abstract Class<T> type();

    @Nonnull
    public final T get() {
        return value;
    }

    @Nonnull
    public final <U, W extends Value<U, W>> W map(
            @Nonnull final Function<? super T, ? extends W> mapper) {
        requireNonNull(mapper, "mapper");
        return mapper.apply(value);
    }

    @SuppressWarnings({"CloneDoesntCallSuperClone", "unchecked"})
    @Override
    public final V clone() {
        return (V) this;
    }

    @Override
    public final String toString() {
        return "<" + value + ">";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Value value1 = (Value) o;

        if (!value.equals(value1.value)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
