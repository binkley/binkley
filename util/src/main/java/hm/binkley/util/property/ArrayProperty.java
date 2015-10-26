package hm.binkley.util.property;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.annotation.Nonnull;

@EqualsAndHashCode
@RequiredArgsConstructor
@ToString(includeFieldNames = false)
final class ArrayProperty<T>
        implements Property<T> {
    private final T[] array;
    private final int index;

    @Nonnull
    @Override
    public T get() {
        return array[index];
    }

    @Override
    public void set(@Nonnull final T value) {
        array[index] = value;
    }
}
