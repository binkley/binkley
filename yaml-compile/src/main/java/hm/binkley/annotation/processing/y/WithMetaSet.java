package hm.binkley.annotation.processing.y;

import javax.annotation.Nonnull;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * {@code WithMetaSet} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
class WithMetaSet
        extends AbstractSet<Entry<String, Map<String, Object>>> {
    private final Set<Entry<String, Map<String, Object>>> raw;
    private final int size;

    WithMetaSet(final Set<Entry<String, Map<String, Object>>> raw,
            final int size) {
        this.raw = raw;
        this.size = size;
    }

    @Nonnull
    @Override
    public Iterator<Entry<String, Map<String, Object>>> iterator() {
        return new WithMetaIterator(raw.iterator());
    }

    @Override
    public int size() {
        return size;
    }
}
