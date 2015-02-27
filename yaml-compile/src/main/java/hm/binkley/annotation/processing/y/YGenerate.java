package hm.binkley.annotation.processing.y;

import hm.binkley.annotation.processing.LoadedTemplate;
import hm.binkley.annotation.processing.LoadedYaml;
import hm.binkley.annotation.processing.Names;
import hm.binkley.annotation.processing.YamlGenerateMesseger;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nonnull;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;

import static hm.binkley.annotation.processing.Utils.typeFor;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * {@code YGenerate} models the differences between enums and classes for code
 * generation.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Not clear this is the best approach
 */
public enum YGenerate {
    YENUM() {
        @Override
        protected void putInto(final ZisZuper names,
                final Map<String, Object> model,
                final List<? extends YBlock> blocks) {
            model.put("type", "Enum");
            model.put("values", unmodifiableMap(new EnumsMap(blocks)));
        }

        @Override
        YBlock block(final Yaml yaml, final ZisZuper names,
                final Entry<String, Map<String, Object>> raw) {
            return new YEnum(yaml, raw);
        }
    }, YCLASS() {
        @Override
        protected void putInto(final ZisZuper names,
                final Map<String, Object> model,
                final List<? extends YBlock> blocks) {
            model.put("type", "Class");
            model.put("parent", names.parent());
            model.put("parentKind", names.kind());
            model.put("methods",
                    unmodifiableMap(new MethodsMap(names, blocks)));
        }

        @Override
        YBlock block(final Yaml yaml, final ZisZuper names,
                final Entry<String, Map<String, Object>> raw) {
            // TODO: Some less gross place for this global
            final YMethod method = new YMethod(yaml, raw);
            YModel.methods.
                    computeIfAbsent(names, ignored -> new ArrayList<>()).
                    add(method);
            return method;
        }
    };

    @Nonnull
    static YGenerate from(@Nonnull final ZisZuper names) {
        final Names zuper = names.zuper;
        return null == zuper || !"Enum".equals(zuper.name) ? YCLASS : YENUM;
    }

    abstract YBlock block(final Yaml yaml, final ZisZuper names,
            final Entry<String, Map<String, Object>> raw);

    protected abstract void putInto(final ZisZuper names,
            final Map<String, Object> model,
            final List<? extends YBlock> blocks);

    public YType yType(final Yaml yaml, final LoadedTemplate template,
            final LoadedYaml loaded, final ZisZuper names,
            final Map<String, Map<String, Object>> raw,
            final Consumer<Function<YamlGenerateMesseger, YamlGenerateMesseger>> out) {
        return new YType(getClass().getName(), yaml, template, loaded, names,
                this, new WithMetaMap(null == raw ? emptyMap() : raw), out);
    }

    private static List<Map<String, Object>> seq(final Object value) {
        return null == value ? null : ((List<Object>) value).stream().
                map(a -> new LinkedHashMap<String, Object>() {{
                    put("type", typeFor(a));
                    put("value", a);
                }}).
                collect(toList());
    }

    private static Map<String, Map<String, Object>> pairs(
            final Object value) {
        return null == value ? null
                : ((Map<String, Object>) value).entrySet().stream().
                        map(e -> new SimpleImmutableEntry<>(e.getKey(),
                                new LinkedHashMap<String, Object>() {{
                                    put("type", typeFor(e.getValue()));
                                    put("value", e.getValue());
                                }})).
                        collect(toMap(Map.Entry::getKey,
                                Map.Entry::getValue));
    }

    private static class EnumsMap
            extends LinkedHashMap<String, Object> {
        EnumsMap(final List<? extends YBlock> blocks) {
            for (final YEnum block : (List<YEnum>) blocks)
                put(block.name, new EnumMap(block));
        }
    }

    private static class EnumMap
            extends LinkedHashMap<String, Object> {
        EnumMap(final YEnum block) {
            block.putInto(this);
        }
    }

    private static class MethodsMap
            extends LinkedHashMap<String, Object> {
        MethodsMap(final ZisZuper names,
                final List<? extends YBlock> blocks) {
            for (final YMethod block : (List<YMethod>) blocks)
                put(block.name, new MethodMap(names, block));
        }
    }

    private static class MethodMap
            extends LinkedHashMap<String, Object> {
        MethodMap(final ZisZuper names, final YMethod block) {
            block.putInto(this);
            put("override", names.overriddenBy(block));
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
        }
    }
}
