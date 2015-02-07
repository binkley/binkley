package hm.binkley.annotation.processing;

import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static hm.binkley.annotation.processing.MethodDescription.methodDescription;
import static hm.binkley.annotation.processing.Utils.cast;
import static hm.binkley.annotation.processing.Utils.typeFor;

/**
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
enum YGenerate {
    ENUM("Enum", "values") {
        @Override
        protected void updateModel(final Map<String, Object> model,
                final ZisZuper names) {
            // Do nothing
        }

        @Override
        protected void updateBlock(final String name,
                final Map<String, Object> block, final ZisZuper names,
                final Map<String, Map<String, Map<String, Object>>> methods) {
            // Do nothing
        }
    },
    CLAZZ("Class", "methods") {
        @Override
        protected void updateModel(final Map<String, Object> model,
                final ZisZuper names) {
            model.put("parent", names.parent());
            model.put("parentKind", names.kind());
        }

        @Override
        protected void updateBlock(final String name,
                final Map<String, Object> block, final ZisZuper names,
                final Map<String, Map<String, Map<String, Object>>> methods) {
            final MethodDescription method = methodDescription(name,
                    (String) block.get("type"), block.get("value"));
            block.put("name", method.name);
            block.put("type", method.type);
            if ("seq".equals(method.type)) {
                final List<?> elements = cast(method.value);
                final List<Map<String, ?>> value = new ArrayList<>(
                        elements.size());
                elements.forEach(v -> value
                        .add(ImmutableMap.of("value", v, "type", typeFor(v))));
                block.put("value", value);
            } else if ("pairs".equals(method.type)) {
                final Map<String, ?> elements = cast(method.value);
                final Map<String, Map<String, ?>> value = new LinkedHashMap<>(
                        elements.size());
                elements.forEach((k, v) -> value.put(k,
                        ImmutableMap.of("value", v, "type", typeFor(v))));
                block.put("value", value);
            } else
                block.put("value", method.value);
            block.put("override", names.overridden(methods, name));
        }
    };

    public final String typeKey;
    public final String loopKey;

    YGenerate(final String typeKey, final String loopKey) {
        this.typeKey = typeKey;
        this.loopKey = loopKey;
    }

    protected abstract void updateModel(final Map<String, Object> model,
            final ZisZuper names);

    protected abstract void updateBlock(final String name,
            final Map<String, Object> block, final ZisZuper names,
            final Map<String, Map<String, Map<String, Object>>> methods);

    @Nonnull
    public static YGenerate from(@Nonnull final ZisZuper names) {
        final Names zuper = names.zuper;
        return null == zuper || !"Enum".equals(zuper.name) ? CLAZZ : ENUM;
    }
}
