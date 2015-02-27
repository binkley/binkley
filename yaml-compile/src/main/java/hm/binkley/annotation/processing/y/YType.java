package hm.binkley.annotation.processing.y;

import hm.binkley.annotation.processing.LoadedTemplate;
import hm.binkley.annotation.processing.LoadedYaml;
import hm.binkley.annotation.processing.YamlGenerateMesseger;
import hm.binkley.util.Listable;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

/**
 * {@code YType} models enum types and classes.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public final class YType
        extends YDocumented
        implements Listable<YBlock> {
    /** The class name and parent details, never missing. */
    @Nonnull
    public final ZisZuper names;
    /** Code generation specific to enum or class, never missing. */
    @Nonnull
    public final YGenerate type;
    /** The code generation comments, never missing. */
    @Nonnull
    public final String comments;
    /** The list of enum values or class methods, never missing. */
    @Nonnull
    public final List<YBlock> blocks;
    private final String generator;

    YType(final String generator, final Yaml yaml,
            final LoadedTemplate template, final LoadedYaml loaded,
            @Nonnull final ZisZuper names, @Nonnull final YGenerate type,
            final Map<String, Map<String, Object>> raw,
            final Consumer<Function<YamlGenerateMesseger, YamlGenerateMesseger>> out) {
        super(names.zis.name, (String) raw.get(".meta").get("doc"), yaml,
                raw.get(".meta"));
        this.generator = generator;
        this.names = names;
        this.type = type;
        // TODO: Regularize toString() vs where()
        comments = format("From '%s' using '%s'", loaded.where(), template);
        blocks = yBlocks(yaml, names, type, raw, out);
    }

    @Nonnull
    @Override
    public List<YBlock> list() {
        return unmodifiableList(blocks);
    }

    public Map<String, ?> asMap() {
        return new LinkedHashMap<String, Object>() {{
            put("generator", generator);
            put("now", Instant.now().toString());
            put("comments", comments);
            put("package", names.zis.packaj);
            putInto(this);
            type.putInto(names, this, blocks);
        }};
    }

    private static List<YBlock> yBlocks(final Yaml yaml, final ZisZuper names,
            final YGenerate type, final Map<String, Map<String, Object>> raw,
            final Consumer<Function<YamlGenerateMesseger, YamlGenerateMesseger>> out) {
        // Create this eagerly so even empty classes have an entry
        YModel.methods.putIfAbsent(names, new ArrayList<>());
        // Caution - peek for side effect; DO NOT parallelize
        return unmodifiableList(raw.entrySet().stream().
                filter(e -> !".meta".equals(e.getKey())).
                peek(e -> out.accept(o -> o.atYamlBlock(e))).
                map(e -> type.block(yaml, names, e)).
                collect(toList()));
    }
}
