package hm.binkley.annotation.processing.y;

import hm.binkley.annotation.processing.Names;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nonnull;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;

import static hm.binkley.annotation.processing.Utils.typeFor;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * {@code YGenerate} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
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
                        put("override", names.override(block));
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
    };

    private final BiFunction<Yaml, Entry<String, Map<String, Object>>, YBlock>
            ctor;

    YGenerate(
            final BiFunction<Yaml, Entry<String, Map<String, Object>>, YBlock> ctor) {
        this.ctor = ctor;
    }

    public final YBlock block(final Yaml yaml,
            final Entry<String, Map<String, Object>> raw) {
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
}
