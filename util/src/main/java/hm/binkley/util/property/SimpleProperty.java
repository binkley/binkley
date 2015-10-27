package hm.binkley.util.property;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.annotation.Nullable;

/**
 * {@code SimpleProperty} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 */
@AllArgsConstructor(staticName = "valueOf")
@EqualsAndHashCode
@ToString(includeFieldNames = false)
public final class SimpleProperty<T>
        implements Property<T> {
    @Nullable
    private T value;

    @Nullable
    @Override
    public T get() {
        return value;
    }

    @Override
    public void set(@Nullable final T value) {
        this.value = value;
    }
}
