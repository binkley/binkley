package hm.binkley.util.property;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@EqualsAndHashCode
@RequiredArgsConstructor
@ToString(includeFieldNames = false)
final class ListProperty<T>
        implements Property<T> {
    @Nonnull
    private final List<T> list;
    private final int index;

    @Nullable
    @Override
    public T get() {
        return list.get(index);
    }

    @Override
    public void set(@Nullable final T value) {
        list.set(index, value);
    }
}
