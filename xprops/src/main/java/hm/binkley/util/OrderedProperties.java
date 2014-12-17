/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util;

import javax.annotation.Nonnull;
import java.util.AbstractCollection;
import java.util.AbstractMap.SimpleEntry;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import static hm.binkley.util.OrderedProperties.Ordering.DEFINED;
import static java.util.Collections.enumeration;

/**
 * {@code OrderedProperties} is a JDK {@code Properties} with a defined ordering.
 * <p>
 * Note {@code equals} and {@code hashCode} are from {@code Properties}, not
 * overriden.  Generally JDK properties should not be compared or used as map
 * keys.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 * @todo Get Ordering to be Comparator and get Java 8 methods for free
 */
public class OrderedProperties
        extends Properties {
    /** Key ordering for JDK properties. */
    public enum Ordering {
        /** Orders keys by the encounter order as properties are defined. */
        DEFINED {
            @Override
            protected Set<Object> from() {
                return new LinkedHashSet<>();
            }
        },
        /** Orders keys by update order as properties are modified. */
        UPDATED {
            @Override
            protected Set<Object> from() {
                return new UpdatedSet<>();
            }
        },
        /** Orders keys by their natural sorting order. */
        NATURAL {
            @Override
            protected Set<Object> from() {
                return new TreeSet<>();
            }
        };

        protected abstract Set<Object> from();
    }

    /**
     * Maintains key order.  This should be a {@code SortedSet}, however {@code
     * LinkedHashSet} provides an ordering without implementing {@code
     * SortedSet} so fall back on the superinterface.
     */
    private final Set<Object> keys;

    public OrderedProperties() {
        this(DEFINED);
    }

    public OrderedProperties(@Nonnull final Ordering ordering) {
        this(ordering.from());
    }

    public OrderedProperties(@Nonnull final Comparator<? super Object> ordering) {
        this(new TreeSet<>(ordering));
    }

    private OrderedProperties(final Set<Object> keys) {
        this.keys = keys;
    }

    @Override
    public Enumeration<?> propertyNames() {
        return enumeration(keySet());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> stringPropertyNames() {
        return (Set) keySet();
    }

    @Override
    public synchronized Enumeration<Object> keys() {
        return enumeration(keySet());
    }

    @Override
    public synchronized Enumeration<Object> elements() {
        return enumeration(values());
    }

    @Override
    public synchronized Object put(final Object key, final Object value) {
        keys.add(key);
        return super.put(key, value);
    }

    @Override
    public synchronized Object remove(final Object key) {
        keys.remove(key);
        return super.remove(key);
    }

    @Nonnull
    @Override
    public Set<Object> keySet() {
        return new Keys();
    }

    @Nonnull
    @Override
    public Set<Entry<Object, Object>> entrySet() {
        return new Entries();
    }

    @Nonnull
    @Override
    public Collection<Object> values() {
        return new Values();
    }

    private static final class UpdatedSet<T>
            extends LinkedHashSet<T> {
        @Override
        public boolean add(final T element) {
            final boolean found = remove(element);
            super.add(element);
            return found;
        }
    }

    private final class Keys
            extends AbstractSet<Object> {
        @Nonnull
        @Override
        public Iterator<Object> iterator() {
            return new KeyIterator();
        }

        @Override
        public int size() {
            return OrderedProperties.this.size();
        }

        @Override
        public boolean remove(final Object o) {
            return null != OrderedProperties.this.remove(o);
        }
    }

    private final class KeyIterator
            implements Iterator<Object> {
        private final Iterator<Object> kit = keys.iterator();
        private Object key;

        @Override
        public boolean hasNext() {
            return kit.hasNext();
        }

        @Override
        public Object next() {
            return key = kit.next();
        }

        @Override
        public void remove() {
            OrderedProperties.this.remove(key);
        }
    }

    private final class Entries
            extends AbstractSet<Entry<Object, Object>> {
        @Nonnull
        @Override
        public Iterator<Entry<Object, Object>> iterator() {
            return new EntryIterator();
        }

        @Override
        public int size() {
            return OrderedProperties.this.size();
        }

        @Override
        public boolean add(final Entry<Object, Object> entry) {
            final Object newValue = entry.getValue();
            return Objects.equals(newValue, put(entry.getKey(), newValue));
        }

        @Override
        public boolean remove(final Object o) {
            if (!(o instanceof Entry))
                return false;
            final Entry entry = (Entry) o;
            return null != OrderedProperties.this.remove(entry.getKey());
        }
    }

    private final class EntryIterator
            implements Iterator<Entry<Object, Object>> {
        private final Iterator<Object> kit = keys.iterator();
        private Object key;

        @Override
        public boolean hasNext() {
            return kit.hasNext();
        }

        @Override
        public Entry<Object, Object> next() {
            key = kit.next();
            return new SimpleEntry<>(key, get(key));
        }

        @Override
        public void remove() {
            OrderedProperties.this.remove(key);
        }
    }

    private final class Values
            extends AbstractCollection<Object> {
        @Nonnull
        @Override
        public Iterator<Object> iterator() {
            return new ValueIterator();
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean remove(final Object o) {
            for (final Object key : keys)
                if (Objects.equals(o, get(key))) {
                    OrderedProperties.this.remove(key);
                    return true;
                }
            return false;
        }
    }

    private final class ValueIterator
            implements Iterator<Object> {
        private final Iterator<Object> kit = keys.iterator();
        private Object key;

        @Override
        public boolean hasNext() {
            return kit.hasNext();
        }

        @Override
        public Object next() {
            key = kit.next();
            return get(key);
        }

        @Override
        public void remove() {
            OrderedProperties.this.remove(key);
        }
    }
}
