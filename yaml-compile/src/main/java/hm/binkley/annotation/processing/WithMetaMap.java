package hm.binkley.annotation.processing;

import javax.annotation.Nonnull;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

/**
 * {@code WithMetaMap} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
class WithMetaMap
        extends AbstractMap<String, Map<String, Object>> {
    private final Map<String, Map<String, Object>> raw;

    WithMetaMap(final Map<String, Map<String, Object>> raw) {
        this.raw = raw;
    }

    @Nonnull
    @Override
    public Set<Entry<String, Map<String, Object>>> entrySet() {
        return new WithMetaSet(raw.entrySet(),
                raw.containsKey(".meta") ? raw.size() : raw.size() + 1);
    }
}
