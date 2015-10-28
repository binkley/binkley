package hm.binkley.util.property;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@EqualsAndHashCode
@RequiredArgsConstructor
@ToString(includeFieldNames = false)
final class ArrayProperty<T>
        implements Property<T> {
    @Nonnull
    private final T[] array;
    private final int index;

    @Nullable
    @Override
    public T get() {
        return array[index];
    }

    @Override
    public void set(@Nullable final T value) {
        array[index] = value;
    }
}
