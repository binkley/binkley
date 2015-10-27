package hm.binkley.util.property;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.annotation.Nullable;
import java.util.Map;

@EqualsAndHashCode
@RequiredArgsConstructor
@ToString(includeFieldNames = false)
final class MapProperty<T, K>
        implements Property<T> {
    private final Map<? super K, T> map;
    private final K key;

    @Nullable
    @Override
    public T get() {
        return map.get(key);
    }

    @Override
    public void set(@Nullable final T value) {
        map.put(key, value);
    }
}
