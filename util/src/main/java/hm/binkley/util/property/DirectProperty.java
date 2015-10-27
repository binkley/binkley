package hm.binkley.util.property;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.annotation.Nullable;

/** todo {@link #valueOf(Object)} is not typing correctly */
@AllArgsConstructor(staticName = "valueOf")
@EqualsAndHashCode
@ToString(includeFieldNames = false)
final class DirectProperty<T>
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
