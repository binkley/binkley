package hm.binkley.annotation.processing;

import hm.binkley.annotation.processing.YModel.YType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

import static hm.binkley.annotation.processing.Utils.typeFor;
import static hm.binkley.annotation.processing.Utils.valueFor;
import static hm.binkley.annotation.processing.YGenerate.ENUM;
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
public final class YModel
        extends AbstractList<YType> {
    private static final Map<Names, Map<String, YMethod>> methods
            = new HashMap<>();

    private final List<YType> types;
    private final Consumer<Function<YamlGenerateMesseger, YamlGenerateMesseger>>
            out;

    public YModel(@Nonnull final Element root, @Nonnull final Name packaj,
            @Nonnull final Map<String, Map<String, Map<String, Object>>> raw,
            @Nonnull
            final Consumer<Function<YamlGenerateMesseger, YamlGenerateMesseger>> out) {
        this.out = out;
        // No need to wrap in immutableList - AbstractList base is immutable
        // Caution - peek for side effect; DO NOT parallelize
        types = raw.entrySet().stream().
                peek(e -> out.accept(o -> o.atYamlBlock(e))).
                map(e -> yType(root, packaj, e)).
                collect(toList());
    }

    @Override
    public YType get(final int index) {
        return types.get(index);
    }

    @Override
    public int size() {
        return types.size();
    }

    @Override
    public Iterator<YType> iterator() {
        return types.iterator();
    }

    private static abstract class YDocumented {
        private static final Pattern DQUOTE = compile("\"");
        public final String name;
        public final String doc;
        public final String escapedDoc;

        protected YDocumented(final String name, final String doc) {
            this.name = name;
            this.doc = doc;
            escapedDoc = null == doc ? null
                    : DQUOTE.matcher(escapeJava(doc)).replaceAll("\\\"");
        }
    }

    private YType yType(final Element root, final Name packaj,
            final Entry<String, Map<String, Map<String, Object>>> e) {
        final ZisZuper names = ZisZuper.from(packaj, e.getKey(), root);
        final YGenerate type = YGenerate.from(names);
        final Map<String, Map<String, Object>> rawValue = e.getValue();
        final WithMetaMap value = new WithMetaMap(
                null == rawValue ? emptyMap() : rawValue);
        return new YType(names, type, value);
    }

    private List<YNode> yNodes(final Map<String, Map<String, Object>> raw,
            final Function<Entry<String, Map<String, Object>>, YNode> ctor) {
        // Caution - peek for side effect; DO NOT parallelize
        return unmodifiableList(raw.entrySet().stream().
                filter(e -> !".meta".equals(e.getKey())).
                peek(e -> out.accept(o -> o.atYamlBlock(e))).
                map(ctor::apply).
                collect(toList()));
    }

    public final class YType
            extends YDocumented
            implements Iterable<YNode> {
        public final ZisZuper names;
        public final YGenerate type;
        public final List<YNode> nodes;

        protected YType(final ZisZuper names, final YGenerate type,
                final Map<String, Map<String, Object>> raw) {
            super(names.zis.fullName, (String) raw.get(".meta").get("doc"));
            this.names = names;
            this.type = type;
            nodes = yNodes(raw, ENUM == type ? YValue::new : YMethod::new);
        }

        @Override
        public final Iterator<YNode> iterator() {
            return nodes.iterator();
        }
    }

    private static abstract class YNode
            extends YDocumented {
        protected YNode(final Map.Entry<String, Map<String, Object>> raw) {
            super(raw.getKey(), doc(raw));
        }

        private static String doc(
                final Map.Entry<String, Map<String, Object>> raw) {
            final Map<String, Object> properties = raw.getValue();
            return null == properties ? null : (String) properties.get("doc");
        }
    }

    public static final class YValue
            extends YNode {
        protected YValue(final Map.Entry<String, Map<String, Object>> raw) {
            super(raw);
        }
    }

    public static final class YMethod
            extends YNode
            implements Iterable<YProperty> {
        public final String rtype;
        public final Object value;
        private final List<YProperty> properties;

        private YMethod(final Map.Entry<String, Map<String, Object>> raw) {
            super(raw);
            final Map<String, Object> values = raw.getValue();
            rtype = (String) values.computeIfAbsent("type",
                    k -> typeFor(values.get("value")));
            value = values.computeIfAbsent("value", k -> valueFor(rtype));
            properties = unmodifiableList(values.entrySet().stream().
                    map(YProperty::new).
                    collect(toList()));
        }

        @Nonnull
        @Override
        public Iterator<YProperty> iterator() {
            return properties.iterator();
        }
    }

    public static final class YProperty {
        @Nonnull
        public final String name;
        @Nullable
        public final Object value;

        private YProperty(final Map.Entry<String, Object> raw) {
            name = raw.getKey();
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
