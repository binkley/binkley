package hm.binkley.util.property;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.annotation.Nonnull;
import java.util.List;

@EqualsAndHashCode
@RequiredArgsConstructor
@ToString(includeFieldNames = false)
final class ListProperty<T>
        implements Property<T> {
    private final List<T> list;
    private final int index;

    @Nonnull
    @Override
    public T get() {
        return list.get(index);
    }

    @Override
    public void set(@Nonnull final T value) {
        list.set(index, value);
    }
}
