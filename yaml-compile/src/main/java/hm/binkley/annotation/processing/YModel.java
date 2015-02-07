package hm.binkley.annotation.processing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;

/**
 * Represents YAML class/enum definitions immutably and accessible from
 * FreeMarker.  Typical: <pre>
 * Foo:
 *   .meta:
 *     doc: I am the one and only Foo!
 *   bar:
 *     doc: I am bar
 *     value: 0</pre>
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Enums
 */
public final class YModel {
    private static abstract class Documented {
        private static final Pattern DQUOTE = compile("\"");
        public final String name;
        public final String doc;
        public final String escapedDoc;

        protected Documented(final String name, final String doc) {
            this.name = name;
            this.doc = doc;
            escapedDoc = null == doc ? null
                    : DQUOTE.matcher(escapeJava(doc)).replaceAll("\\\"");
        }
    }

    public static List<YClass> classes(@Nonnull final Element root,
            @Nonnull final Name packaj, @Nonnull
    final Map<String, Map<String, Map<String, Object>>> raw) {
        return unmodifiableList(raw.entrySet().stream().
                map(e -> YClass.of(ZisZuper.from(packaj, e.getKey(), root),
                        e.getValue())).
                collect(toList()));
    }

    public static final class YClass
            extends Documented
            implements Iterable<YMethod> {
        public final ZisZuper names;
        public final YGenerate type;
        private final List<YMethod> methods;

        @Nonnull
        public static YClass of(@Nonnull final ZisZuper types,
                @Nullable final Map<String, Map<String, Object>> raw) {
            return new YClass(types,
                    new WithMetaMap(null == raw ? emptyMap() : raw));
        }

        private YClass(final ZisZuper names,
                final Map<String, Map<String, Object>> raw) {
            super(names.zis.fullName, (String) raw.get(".meta").get("doc"));
            this.names = names;
            type = YGenerate.from(names);
            this.methods = unmodifiableList(raw.entrySet().stream().
                    filter(e -> ".meta".equals(e.getKey())).
                    map(YMethod::new).
                    collect(toList()));
        }

        @Nonnull
        @Override
        public Iterator<YMethod> iterator() {
            return methods.iterator();
        }
    }

    public static final class YMethod
            extends Documented
            implements Iterable<YProperty> {
        private final List<YProperty> properties;

        private YMethod(final Map.Entry<String, Map<String, Object>> raw) {
            super(raw.getKey(), (String) raw.getValue().get("doc"));
            properties = unmodifiableList(raw.getValue().entrySet().stream().
                    map(YProperty::new).
                    collect(toList()));
        }

        @Nonnull
        @Override
        public Iterator<YProperty> iterator() {
            return properties.iterator();
        }
    }

    public static final class YProperty
            extends Documented {
        @Nullable
        public final Object value;

        private YProperty(final Map.Entry<String, Object> raw) {
            super(raw.getKey(), null);
            this.value = raw.getValue();
        }
    }

    private static class WithMetaMap
            extends AbstractMap<String, Map<String, Object>> {
        private final Map<String, Map<String, Object>> raw;

        private WithMetaMap(final Map<String, Map<String, Object>> raw) {
            this.raw = raw;
        }

        @Nonnull
        @Override
        public Set<Entry<String, Map<String, Object>>> entrySet() {
            return new WithMetaSet(raw.entrySet(),
                    raw.containsKey(".meta") ? raw.size() : raw.size() + 1);
        }
    }

    private static class WithMetaSet
            extends AbstractSet<Entry<String, Map<String, Object>>> {
        private final Set<Entry<String, Map<String, Object>>> raw;
        private final int size;

        private WithMetaSet(final Set<Entry<String, Map<String, Object>>> raw,
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

    private static class WithMetaIterator
            implements Iterator<Entry<String, Map<String, Object>>> {
        private static final Entry<String, Map<String, Object>> meta
                = new SimpleImmutableEntry<>(".meta", emptyMap());

        private final Iterator<Entry<String, Map<String, Object>>> it;

        private boolean sawMeta;

        private WithMetaIterator(
                final Iterator<Entry<String, Map<String, Object>>> it) {
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
}
