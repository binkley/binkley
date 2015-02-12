package hm.binkley.annotation.processing;

import hm.binkley.annotation.processing.YModel.YType;
import hm.binkley.util.YamlHelper;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

import static hm.binkley.annotation.processing.Utils.typeFor;
import static hm.binkley.annotation.processing.Utils.valueFor;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.Collections.unmodifiableList;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;
import static org.yaml.snakeyaml.DumperOptions.FlowStyle.FLOW;
import static org.yaml.snakeyaml.DumperOptions.ScalarStyle.PLAIN;

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
 * @todo Replace List subtype with Stream factory method?
 */
public final class YModel
        extends AbstractList<YType> {
    @Nonnull
    private final Yaml yaml = YamlHelper.builder().build(dumper -> {
        dumper.setDefaultFlowStyle(FLOW);
        dumper.setDefaultScalarStyle(PLAIN);
        dumper.setTimeZone(null);
        dumper.setWidth(Integer.MAX_VALUE);
    });
    private final Consumer<Function<YamlGenerateMesseger, YamlGenerateMesseger>>
            out;
    private final List<YType> types;

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
        public final List<String> definition;

        protected YDocumented(final String name, final String doc,
                final Yaml yaml, final Map<String, ?> raw) {
            this.name = name;
            this.doc = doc;
            escapedDoc = null == doc ? null
                    : DQUOTE.matcher(escapeJava(doc)).replaceAll("\\\"");
            this.definition = toAnnotationValue(yaml, raw);
        }

        private static List<String> toAnnotationValue(final Yaml yaml,
                final Map<String, ?> props) {
            return props.entrySet().stream().
                    map(e -> singletonMap(e.getKey(), e.getValue())).
                    map(e -> toQuotedYaml(yaml, e)).
                    collect(toList());
        }

        private static String toQuotedYaml(final Yaml yaml,
                final Object value) {
            final String dumped = yaml.dump(value);
            return format("\"%s\"",
                    escapeJava(dumped.substring(0, dumped.length() - 1)));
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

    public enum YGenerate {
        ENUM(YEnum::new), CLASS(YMethod::new);

        private final BiFunction<Yaml, Entry<String, Map<String, Object>>, YNode>
                ctor;

        YGenerate(
                final BiFunction<Yaml, Entry<String, Map<String, Object>>, YNode> ctor) {
            this.ctor = ctor;
        }

        public final YNode node(final Yaml yaml,
                final Entry<String, Map<String, Object>> raw) {
            return ctor.apply(yaml, raw);
        }

        @Nonnull
        public static YGenerate from(@Nonnull final ZisZuper names) {
            final Names zuper = names.zuper;
            return null == zuper || !"Enum".equals(zuper.name) ? CLASS : ENUM;
        }

    }

    public final class YType
            extends YDocumented
            implements Iterable<YNode> {
        public final ZisZuper names;
        public final YGenerate type;
        public final List<YNode> nodes;

        protected YType(final ZisZuper names, final YGenerate type,
                final Map<String, Map<String, Object>> raw) {
            super(names.zis.fullName, (String) raw.get(".meta").get("doc"),
                    yaml, raw);
            this.names = names;
            this.type = type;
            nodes = yNodes(type, raw);
        }

        @Override
        public final Iterator<YNode> iterator() {
            return nodes.iterator();
        }

        private List<YNode> yNodes(final YGenerate type,
                final Map<String, Map<String, Object>> raw) {
            // Caution - peek for side effect; DO NOT parallelize
            return unmodifiableList(raw.entrySet().stream().
                    filter(e -> !".meta".equals(e.getKey())).
                    peek(e -> out.accept(o -> o.atYamlBlock(e))).
                    map(e -> type.node(yaml, e)).
                    collect(toList()));
        }
    }

    public static final class YEnum
            extends YNode {
        protected YEnum(final Yaml yaml,
                final Map.Entry<String, Map<String, Object>> raw) {
            super(yaml, raw);
        }

        @Override
        public String toString() {
            return format("YEnum{%s}", name);
        }
    }

    public static final class YMethod
            extends YNode
            implements Iterable<YProperty> {
        public final String rtype;
        public final Object value;
        private final List<YProperty> properties;

        private YMethod(final Yaml yaml,
                final Map.Entry<String, Map<String, Object>> raw) {
            super(yaml, raw);
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

        @Override
        public String toString() {
            return format("YMethod:%s{%s}=%s", rtype, name, value);
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

    private static abstract class YNode
            extends YDocumented {
        protected YNode(final Yaml yaml,
                final Map.Entry<String, Map<String, Object>> raw) {
            super(raw.getKey(), doc(raw), yaml,
                    singletonMap(raw.getKey(), raw.getValue()));
        }

        private static String doc(
                final Map.Entry<String, Map<String, Object>> raw) {
            final Map<String, Object> properties = raw.getValue();
            return null == properties ? null : (String) properties.get("doc");
        }

        @Override
        public abstract String toString();
    }
}
