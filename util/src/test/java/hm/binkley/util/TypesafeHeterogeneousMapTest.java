package hm.binkley.util;

import hm.binkley.util.TypesafeHeterogeneousMap.Key;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@code TypesafeHeterogeneousMapTest} tests {@link TypesafeHeterogeneousMap}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public final class TypesafeHeterogeneousMapTest {
    private TypesafeHeterogeneousMap<K, V> map;

    @Before
    public void setUp() {
        map = new TypesafeHeterogeneousMap<>();
    }

    @Test
    public void shouldWorkSimply() {
        final K key = new K("apple");
        final V value = new V("cart");

        final Key<K, V> k = new Key<>(key, value);
        map.put(k, value);

        assertThat(map.get(k), is(equalTo(value)));
    }

    @Test
    public void shouldWorkWithSubclassedValue() {
        final K key = new K("apple");
        final W value = new W("cart");

        final Key<K, W> k = new Key<>(key, value);
        map.put(k, value);

        assertThat(map.get(k), is(equalTo(value)));
    }

    @Test
    public void shouldWorkWithSubclassedKey() {
        final L key = new L("apple");
        final V value = new V("cart");

        final Key<L, V> k = new Key<>(key, value);
        map.put(k, value);

        assertThat(map.get(k), is(equalTo(value)));
    }

    @Test
    public void shouldWorkSubclassedValue() {
        final K key = new K("apple");
        final W value = new W("cart");

        final Key<K, W> k = new Key<>(key, value);
        map.put(k, value);

        assertThat(map.get(new Key<>(key, V.class)), is(equalTo(value)));
    }

    @Test
    public void shouldPutAllFromMap() {
        final L apple = new L("apple");
        final W core = new W("core");
        final L balti = new L("balti");
        final W more = new W("more");
        final Map<L, W> that = new HashMap<L, W>() {{
            put(apple, core);
            put(balti, more);
        }};

        map.putAll(that);

        assertThat(map.entryMap(), hasEntry(apple, core));
        assertThat(map.entryMap(), hasEntry(balti, more));
    }

    @EqualsAndHashCode
    @ToString
    public static class K {
        @Nonnull
        private final String key;

        private K(final @Nonnull String key) {this.key = key;}
    }

    public static class L
            extends K {
        private L(final @Nonnull String key) {
            super(key);
        }
    }

    @EqualsAndHashCode
    @ToString
    public static class V {
        private final String value;

        private V(final String value) {this.value = value;}
    }

    public static class W
            extends V {
        private W(final String value) {
            super(value);
        }
    }
}
