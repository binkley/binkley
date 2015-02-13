package hm.binkley.annotation.processing;

import hm.binkley.annotation.processing.YModel.YType;
import hm.binkley.annotation.processing.YamlGenerateProcessor.LoadedTemplate;
import hm.binkley.annotation.processing.YamlGenerateProcessor.LoadedYaml;
import hm.binkley.util.YamlHelper;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import java.time.Instant;
import java.util.AbstractList;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.LinkedHashMap;
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
import static java.util.stream.Collectors.toMap;
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
        implements Listable<YType> {
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

    public YModel(@Nonnull final Element root,
            @Nonnull final LoadedTemplate template,
            @Nonnull final LoadedYaml loaded, @Nonnull final Name packaj,
            @Nonnull
            final Consumer<Function<YamlGenerateMesseger, YamlGenerateMesseger>> out) {
        this.out = out;
        // No need to wrap in immutableList - AbstractList base is immutable
        // Caution - peek for side effect; DO NOT parallelize
        types = loaded.what.entrySet().stream().
                peek(e -> out.accept(o -> o.atYamlBlock(e))).
                map(e -> yType(root, loaded, template, packaj, e)).
                collect(toList());
    }

    @Nonnull
    @Override
    public List<YType> list() {
        return new AbstractList<YType>() {
            @Override
            public YType get(final int index) {
                return types.get(index);
            }

            @Override
            public int size() {
                return types.size();
            }
        };
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
            definition = toAnnotationValue(yaml, raw);
        }

        protected void putInto(final Map<String, Object> model) {
            model.put("name", name);
            model.put("doc", doc);
            model.put("escapedDoc", escapedDoc);
            model.put("definition", definition);
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

    @Nullable
    private YType yType(final Element root, final LoadedYaml loaded,
            final LoadedTemplate template, final Name packaj,
            final Entry<String, Map<String, Map<String, Object>>> e) {
        final ZisZuper names = ZisZuper.from(packaj, e.getKey(), root);
        if (null == names) {
            out.accept(o -> {
                o.error("%@ classes have at most one parent for '%s'",
                        e.getKey());
                return o;
            });
            return null;
        }
        final YGenerate type = YGenerate.from(names);
        final Map<String, Map<String, Object>> rawValue = e.getValue();
        final WithMetaMap value = new WithMetaMap(
                null == rawValue ? emptyMap() : rawValue);
        return new YType(template, loaded, names, type, value);
    }

    public enum YGenerate {
        ENUM(YEnum::new) {
            @Override
            protected void putInto(final ZisZuper names,
                    final Map<String, Object> model,
                    final List<? extends YBlock> blocks) {
                model.put("type", "Enum");
                model.put("values", new LinkedHashMap<String, Object>() {{
                    for (final YEnum block : (List<YEnum>) blocks)
                        put(block.name, new LinkedHashMap<String, Object>() {{
                            block.putInto(this);
                        }});
                }});
            }
        }, CLASS(YMethod::new) {
            @Override
            protected void putInto(final ZisZuper names,
                    final Map<String, Object> model,
                    final List<? extends YBlock> blocks) {
                model.put("type", "Class");
                model.put("parent", names.parent());
                model.put("parentKind", names.kind());
                model.put("methods", new LinkedHashMap<String, Object>() {{
                    for (final YMethod block : (List<YMethod>) blocks)
                        put(block.name, new LinkedHashMap<String, Object>() {{
                            block.putInto(this);
                            put("override", override(names, block.name));
                            put("type", block.rtype);
                            switch (block.rtype) {
                            case "seq":
                                put("value", seq(block.value));
                                break;
                            case "pairs":
                                put("value", pairs(block.value));
                                break;
                            default:
                                put("value", block.value);
                            }
                        }});
                }});
            }

            private boolean override(final ZisZuper names,
                    final String method) {
                return names.override(names, method, methods);
            }

            private List<Map<String, Object>> seq(final Object value) {
                return null == value ? null : ((List<Object>) value).stream().
                        map(a -> new LinkedHashMap<String, Object>() {{
                            put("type", typeFor(a));
                            put("value", a);
                        }}).
                        collect(toList());
            }

            private Map<String, Map<String, Object>> pairs(
                    final Object value) {
                return null == value ? null
                        : ((Map<String, Object>) value).entrySet().stream().
                                map(e -> new SimpleImmutableEntry<>(
                                        e.getKey(),
                                        new LinkedHashMap<String, Object>() {{
                                            put("type",
                                                    typeFor(e.getValue()));
                                            put("value", e.getValue());
                                        }})).
                                collect(toMap(SimpleImmutableEntry::getKey,
                                        SimpleImmutableEntry::getValue));
            }
        };

        private static final Map<String, List<String>> methods
                = new LinkedHashMap<>();

        private final BiFunction<Yaml, Map.Entry<String, Map<String, Object>>, YBlock>
                ctor;

        YGenerate(
                final BiFunction<Yaml, Map.Entry<String, Map<String, Object>>, YBlock> ctor) {
            this.ctor = ctor;
        }

        public final YBlock block(final Yaml yaml,
                final Map.Entry<String, Map<String, Object>> raw) {
            return ctor.apply(yaml, raw);
        }

        @Nonnull
        public static YGenerate from(@Nonnull final ZisZuper names) {
            final Names zuper = names.zuper;
            return null == zuper || !"Enum".equals(zuper.name) ? CLASS : ENUM;
        }

        protected abstract void putInto(final ZisZuper names,
                final Map<String, Object> model,
                final List<? extends YBlock> blocks);
    }

    public final class YType
            extends YDocumented
            implements Listable<YBlock> {
        public final ZisZuper names;
        public final YGenerate type;
        public final String comments;
        public final String packaj;
        public final List<YBlock> blocks;

        protected YType(final LoadedTemplate template,
                final LoadedYaml loaded, final ZisZuper names,
                final YGenerate type,
                final Map<String, Map<String, Object>> raw) {
            super(names.zis.name, (String) raw.get(".meta").get("doc"), yaml,
                    raw.get(".meta"));
            this.names = names;
            this.type = type;
            comments = format("From '%s' using '%s'", loaded.where(),
                    template.where());
            packaj = names.zis.packaj;
            blocks = yBlocks(type, raw);
        }

        @Nonnull
        @Override
        public List<YBlock> list() {
            return new AbstractList<YBlock>() {
                @Override
                public YBlock get(final int index) {
                    return blocks.get(index);
                }

                @Override
                public int size() {
                    return blocks.size();
                }
            };
        }

        public Map<String, ?> asMap() {
            return new LinkedHashMap<String, Object>() {{
                // TODO: Why should YModel know about the processor?
                put("generator", YamlGenerateProcessor.class.getName());
                put("now", Instant.now().toString());
                put("comments", comments);
                put("package", names.zis.packaj);
                putInto(this);
                type.putInto(names, this, blocks);
            }};
        }

        private List<YBlock> yBlocks(final YGenerate type,
                final Map<String, Map<String, Object>> raw) {
            // Caution - peek for side effect; DO NOT parallelize
            return unmodifiableList(raw.entrySet().stream().
                    filter(e -> !".meta".equals(e.getKey())).
                    peek(e -> out.accept(o -> o.atYamlBlock(e))).
                    map(e -> type.block(yaml, e)).
                    collect(toList()));
        }
    }

    public static final class YEnum
            extends YBlock {
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
            extends YBlock
            implements Listable<YProperty> {
        // TODO: Work out override here, not in asMap()
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
            properties = values.entrySet().stream().
                    map(YProperty::new).
                    collect(toList());
        }

        @Nonnull
        @Override
        public List<YProperty> list() {
            return new AbstractList<YProperty>() {
                @Override
                public YProperty get(final int index) {
                    return properties.get(index);
                }

                @Override
                public int size() {
                    return properties.size();
                }
            };
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
            value = raw.getValue();
        }
    }

    private static abstract class YBlock
            extends YDocumented {
        protected YBlock(final Yaml yaml,
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
