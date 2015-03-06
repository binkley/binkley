package hm.binkley.util;

import javax.annotation.Nonnull;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * {@code TypesafeHeterogeneousMap} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 * @todo Functions taking value should extend T, not V
 * @see <a href="http://www.codeaffine.com/2015/03/04/map-distinct-value-types-using-java-generics/"><cite>How
 * to Map Distinct Value Types Using Java Generics</cite></a>
 */
@SuppressWarnings("unused")
public final class TypesafeHeterogeneousMap<K, V> {
    private final Map<Key<? extends K, ? extends V>, V> map;

    public TypesafeHeterogeneousMap() {
        map = new LinkedHashMap<>();
    }

    public TypesafeHeterogeneousMap(final int initialCapacity) {
        map = new LinkedHashMap<>(initialCapacity);
    }

    public TypesafeHeterogeneousMap(@Nonnull
    final TypesafeHeterogeneousMap<? extends K, ? extends V> that) {
        map = new LinkedHashMap<>(that.map);
    }

    public TypesafeHeterogeneousMap(
            @Nonnull final Map<? extends K, ? extends V> that) {
        map = new LinkedHashMap<>(that.size());
        putAll(that);
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean containsKey(
            @Nonnull final Key<? extends K, ? extends V> key) {
        return map.containsKey(key);
    }

    public boolean containsKey(final K key) {
        return map.containsKey(dummy(key));
    }

    public boolean containsValue(final V value) {
        return map.containsValue(value);
    }

    public <T extends V> T get(@Nonnull final Key<? extends K, T> key) {
        return key.cast(map.get(key));
    }

    public V get(final K key) {
        return map.get(dummy(key));
    }

    public <T extends V> T put(@Nonnull final Key<? extends K, T> key,
            final T value) {
        return key.cast(map.put(key, value));
    }

    public <T extends V> T put(final K key, final T value) {
        return put(new Key<>(key, value), value);
    }

    public <T extends V> V remove(@Nonnull final Key<? extends K, T> key) {
        return key.cast(map.remove(key));
    }

    public V remove(final K key) {
        return map.remove(dummy(key));
    }

    public void putAll(@Nonnull
    final TypesafeHeterogeneousMap<? extends K, ? extends V> that) {
        map.putAll(that.map);
    }

    public void putAll(@Nonnull final Map<? extends K, ? extends V> that) {
        that.entrySet().stream().
                forEach(e -> put(e.getKey(), e.getValue()));
    }

    public void clear() {
        map.clear();
    }

    @Nonnull
    public Set<Key<? extends K, ? extends V>> keySet() {
        return map.keySet();
    }

    @Nonnull
    public Collection<? extends V> values() {
        return map.values();
    }

    @Nonnull
    public Set<Entry<Key<? extends K, ? extends V>, V>> entrySet() {
        return map.entrySet();
    }

    public <T extends V> T getOrDefault(
            @Nonnull final Key<? extends K, T> key, final T defaultValue) {
        return key.cast(map.getOrDefault(key, defaultValue));
    }

    public V getOrDefault(final K key, final V defaultValue) {
        return getOrDefault(new Key<>(key, defaultValue), defaultValue);
    }

    public void forEach(@Nonnull
    final BiConsumer<? super Key<? extends K, ? extends V>, ? super V> action) {
        map.forEach(action);
    }

    @FunctionalInterface
    public interface BiKeyConsumer<K, V> {
        void accept(final K key, final V value);
    }

    public void forEach(
            @Nonnull final BiKeyConsumer<? super K, ? super V> action) {
        entrySet().stream().
                forEach(e -> action.accept(e.getKey().key, e.getValue()));
    }

    public void replaceAll(@Nonnull
    final BiFunction<? super Key<? extends K, ? extends V>, ? super V, ? extends V> fn) {
        map.replaceAll(fn);
    }

    @FunctionalInterface
    public interface BiKeyFunction<K, V> {
        V apply(final K key, final V value);
    }

    public void replaceAll(@Nonnull final BiKeyFunction<? super K, V> fn) {
        entrySet().stream().
                forEach(e -> e
                        .setValue(fn.apply(e.getKey().key, e.getValue())));
    }

    public <T extends V> T putIfAbsent(@Nonnull final Key<? extends K, T> key,
            final T value) {
        return key.cast(map.putIfAbsent(key, value));
    }

    public V putIfAbsent(final K key, final V value) {
        return putIfAbsent(new Key<>(key, value), value);
    }

    public <T extends V> boolean remove(
            @Nonnull final Key<? extends K, T> key, final T value) {
        return map.remove(key, value);
    }

    public boolean remove(final K key, final V value) {
        return remove(new Key<>(key, value), value);
    }

    public <T extends V> boolean replace(
            @Nonnull final Key<? extends K, T> key, final T oldValue,
            final T newValue) {
        return map.replace(key, oldValue, newValue);
    }

    public boolean replace(final K key, final V oldValue, final V newValue) {
        return replace(new Key<>(key, oldValue), oldValue, newValue);
    }

    public <T extends V> T replace(@Nonnull final Key<? extends K, T> key,
            final T value) {
        return key.cast(map.replace(key, value));
    }

    public V replace(final K key, final V value) {
        return replace(new Key<>(key, value), value);
    }

    public <T extends V> T computeIfAbsent(
            @Nonnull final Key<? extends K, T> key, @Nonnull
    final Function<? super Key<? extends K, ? extends V>, ? extends T> fn) {
        return key.cast(map.computeIfAbsent(key, fn));
    }

    @FunctionalInterface
    public interface KeyFunction<K, V> {
        V apply(final K key);
    }

    /** @todo Rethink, reuse, simplify. */
    public V computeIfAbsent(final K key,
            @Nonnull final KeyFunction<? super K, ? extends V> fn) {
        final V oldValue = get(key);
        if (null != oldValue)
            return oldValue;
        final V newValue = fn.apply(key);
        if (null == newValue)
            return null;
        return putNew(key, newValue);
    }

    public <T extends V> T computeIfPresent(
            @Nonnull final Key<? extends K, T> key, @Nonnull
    final BiFunction<? super Key<? extends K, ? extends V>, ? super V, ? extends T> fn) {
        return key.cast(map.computeIfPresent(key, fn));
    }

    /** @todo Rethink, reuse, simplify. */
    public V computeIfPresent(final K key,
            @Nonnull final BiKeyFunction<? super K, V> fn) {
        final V oldValue = get(key);
        if (null == oldValue)
            return null;
        final V newValue = fn.apply(key, oldValue);
        if (null == newValue)
            return removeOld(key);
        return putNew(key, newValue);
    }

    public <T extends V> T compute(@Nonnull final Key<? extends K, T> key,
            @Nonnull
            final BiFunction<? super Key<? extends K, ? extends V>, ? super V, ? extends V> fn) {
        return key.cast(map.compute(key, fn));
    }

    /** @todo Rethink, reuse, simplify. */
    public V compute(final K key,
            @Nonnull final BiKeyFunction<? super K, V> fn) {
        final V oldValue = get(key);
        final V newValue = fn.apply(key, oldValue);
        if (null == newValue) {
            if (null == oldValue && !containsKey(key))
                return null;
            return removeOld(key);
        }
        return putNew(key, newValue);
    }

    public <T extends V> T merge(@Nonnull final Key<? extends K, T> key,
            final T value,
            @Nonnull final BiFunction<? super V, ? super V, ? extends V> fn) {
        return key.cast(map.merge(key, value, fn));
    }

    public <T extends V> T merge(final K key, final T value,
            @Nonnull final BiFunction<? super V, ? super V, ? extends V> fn) {
        return merge(new Key<>(key, value), value, fn);
    }

    @Nonnull
    public Map<? extends K, ? extends V> entryMap() {
        return new EntryMap();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        final TypesafeHeterogeneousMap<?, ?> that
                = (TypesafeHeterogeneousMap<?, ?>) o;
        return Objects.equals(map, that.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map);
    }

    @Override
    public String toString() {
        return map.toString();
    }

    private V removeOld(final K key) {
        remove(key);
        return null;
    }

    private V putNew(final K key, final V newValue) {
        put(key, newValue);
        return newValue;
    }

    private Key<K, V> dummy(final K key) {
        return new Key<>(key, (Class<V>) null);
    }

    public static final class Key<K, V> {
        private final K key;
        private final Class<V> valueType;

        public Key(final K key, final Class<V> valueType) {
            this.key = key;
            this.valueType = valueType;
        }

        @SuppressWarnings("unchecked")
        public Key(final K key, final V value) {
            this(key, (Class<V>) value.getClass());
        }

        private V cast(final Object value) {
            return valueType.cast(value);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            final Key<?, ?> that = (Key<?, ?>) o;
            return Objects.equals(key, that.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key);
        }

        @Override
        public String toString() {
            return Objects.toString(key) + ":" + valueType;
        }
    }

    private class EntryMap
            extends AbstractMap<K, V> {
        @Nonnull
        @Override
        public Set<Entry<K, V>> entrySet() {
            return new EntrySet();
        }

        @Override
        public V put(final K key, final V value) {
            return map.put(new Key<>(key, value), value);
        }
    }

    private class EntrySet
            extends AbstractSet<Entry<K, V>> {
        @Nonnull
        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        @Override
        public int size() {
            return map.size();
        }

        @Override
        public boolean add(final Entry<K, V> e) {
            return !Objects.equals(e.getValue(),
                    map.put(new Key<>(e.getKey(), e.getValue()),
                            e.getValue()));
        }
    }

    private class EntryIterator
            implements Iterator<Entry<K, V>> {
        private final Iterator<Entry<Key<? extends K, ? extends V>, V>> it
                = map.entrySet().iterator();

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public Entry<K, V> next() {
            return new EntryEntry(it.next());
        }

        @Override
        public void remove() {
            it.remove();
        }
    }

    private class EntryEntry
            implements Entry<K, V> {
        private final Entry<Key<? extends K, ? extends V>, V> next;

        EntryEntry(final Entry<Key<? extends K, ? extends V>, V> next) {
            this.next = next;
        }

        @Override
        public K getKey() {
            return next.getKey().key;
        }

        @Override
        public V getValue() {
            return next.getValue();
        }

        @Override
        public V setValue(final V value) {
            return next.setValue(value);
        }
    }
}
