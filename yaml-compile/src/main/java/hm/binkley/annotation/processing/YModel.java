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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

import static hm.binkley.annotation.processing.Utils.typeFor;
import static hm.binkley.annotation.processing.Utils.valueFor;
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

    public static List<YType> classes(@Nonnull final Element root,
            @Nonnull final Name packaj,
            @Nonnull final Map<String, Map<String, Map<String, Object>>> raw,
            @Nonnull
            final Consumer<Function<YamlGenerateMesseger, YamlGenerateMesseger>> out) {
        return unmodifiableList(raw.entrySet().stream().
                map(e -> {
                    out.accept(o -> o.atYamlBlock(e));

                    final ZisZuper names = ZisZuper
                            .from(packaj, e.getKey(), root);
                    return YGenerate.ENUM == YGenerate.from(names) ? YEnum
                            .of(names, e.getValue(), out)
                            : YClass.of(names, e.getValue(), out);
                }).
                collect(toList()));
    }

    public static abstract class YType
            extends YDocumented {
        public final ZisZuper names;
        public final YGenerate type;

        protected YType(final ZisZuper names,
                final Map<String, Map<String, Object>> raw) {
            super(names.zis.fullName, (String) raw.get(".meta").get("doc"));
            this.names = names;
            type = YGenerate.from(names);
        }
    }

    public static final class YEnum
            extends YType
            implements Iterable<YValue> {
        private final List<YValue> values;

        public static YEnum of(final ZisZuper names,
                final Map<String, Map<String, Object>> raw,
                final Consumer<Function<YamlGenerateMesseger, YamlGenerateMesseger>> out) {
            return new YEnum(names,
                    new WithMetaMap(null == raw ? emptyMap() : raw), out);
        }

        private YEnum(final ZisZuper names,
                final Map<String, Map<String, Object>> raw,
                final Consumer<Function<YamlGenerateMesseger, YamlGenerateMesseger>> out) {
            super(names, raw);
            values = YNode.asList(raw, YValue::new, out);
        }

        @Override
        public Iterator<YValue> iterator() {
            return values.iterator();
        }
    }

    public static final class YClass
            extends YType
            implements Iterable<YMethod> {
        private final List<YMethod> methods;

        @Nonnull
        public static YClass of(@Nonnull final ZisZuper names,
                @Nullable final Map<String, Map<String, Object>> raw,
                final Consumer<Function<YamlGenerateMesseger, YamlGenerateMesseger>> out) {
            return new YClass(names,
                    new WithMetaMap(null == raw ? emptyMap() : raw), out);
        }

        private YClass(final ZisZuper names,
                final Map<String, Map<String, Object>> raw,
                final Consumer<Function<YamlGenerateMesseger, YamlGenerateMesseger>> out) {
            super(names, raw);
            this.methods = YNode.asList(raw, YMethod::new, out);
        }

        @Nonnull
        @Override
        public Iterator<YMethod> iterator() {
            return methods.iterator();
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
            rtype = (String) values
                    .getOrDefault("type", typeFor(values.get("value")));
            value = values.getOrDefault("value", valueFor(rtype));
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

        private static <T extends YNode> List<T> asList(
                final Map<String, Map<String, Object>> raw,
                final Function<Entry<String, Map<String, Object>>, T> ctor,
                final Consumer<Function<YamlGenerateMesseger, YamlGenerateMesseger>> out) {
            return unmodifiableList(raw.entrySet().stream().
                    filter(e -> !".meta".equals(e.getKey())).
                    map(e -> {
                        out.accept(o -> o.atYamlBlock(e));
                        return ctor.apply(e);
                    }).
                    collect(toList()));
        }
    }

    public static final class YProperty
            extends YDocumented {
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
