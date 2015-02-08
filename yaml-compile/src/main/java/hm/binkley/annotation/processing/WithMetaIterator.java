package hm.binkley.annotation.processing;

import javax.annotation.Nonnull;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import static java.util.Collections.emptyMap;

/**
 * {@code WithMetaIterator} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
class WithMetaIterator
        implements Iterator<Entry<String, Map<String, Object>>> {
    private static final Entry<String, Map<String, Object>> meta
            = new SimpleImmutableEntry<>(".meta", emptyMap());

    private final Iterator<Entry<String, Map<String, Object>>> it;

    private boolean sawMeta;

    WithMetaIterator(final Iterator<Entry<String, Map<String, Object>>> it) {
        this.it = it;
    }

    @Override
    public boolean hasNext() {
        return it.hasNext() || !sawMeta;
    }

    @Nonnull
    @Override
    public Entry<String, Map<String, Object>> next() {
        try {
            final Entry<String, Map<String, Object>> next = it.next();
            if (".meta".equals(next.getKey()))
                sawMeta = true;
            return next;
        } catch (final NoSuchElementException e) {
            sawMeta = true;
            return meta;
        }
    }
}
