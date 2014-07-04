package hm.binkley.util.value;

import lombok.EqualsAndHashCode;

import javax.annotation.Nonnull;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * {@code Value} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
@EqualsAndHashCode
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
}
