package hm.binkley.annotation.processing;

import freemarker.cache.URLTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import hm.binkley.annotation.YamlGenerate;
import hm.binkley.util.YamlHelper;
import org.kohsuke.MetaInfServices;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.processing.Messager;
import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static freemarker.template.Configuration.VERSION_2_3_21;
import static freemarker.template.TemplateExceptionHandler.DEBUG_HANDLER;
import static java.lang.String.format;
import static java.lang.System.arraycopy;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.joining;
import static javax.lang.model.SourceVersion.RELEASE_8;
import static javax.lang.model.element.ElementKind.INTERFACE;
import static javax.lang.model.element.ElementKind.PACKAGE;

/**
 * {@code YamlGenerateProcessor} generates Java source enums and classes from
 * YAML descriptions.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 * @todo Much better, less hacky APi for extension
 * @todo Parse "freemarker.version" from Maven to construct latest Version
 * @todo Docstrings on enum values
 * @todo Custom configuration of Freemarker
 */
@SupportedAnnotationTypes("hm.binkley.annotation.YamlGenerate")
@SupportedSourceVersion(RELEASE_8)
@MetaInfServices(Processor.class)
public class YamlGenerateProcessor
        extends
        SingleAnnotationProcessor<YamlGenerate, YamlGenerateMesseger> {
    private static final Pattern space = compile("\\s+");
    private final Map<String, Map<String, Map<String, Object>>> methods
            = new LinkedHashMap<>();
    private final Configuration freemarker;
    private Yaml yaml;
    private Template template;

    // In context, system class loader does not have maven class path
    protected final ResourcePatternResolver loader
            = new PathMatchingResourcePatternResolver(
            YamlGenerateProcessor.class.getClassLoader());

    public YamlGenerateProcessor() {
        super(YamlGenerate.class);
        freemarker = new Configuration(VERSION_2_3_21);
        freemarker.setTemplateLoader(new ResourceTemplateLoader());
        freemarker.setDefaultEncoding(UTF_8.name());
        // Dump stacktrace as only called during compilation, not runtime
        freemarker.setTemplateExceptionHandler(DEBUG_HANDLER);
    }

    /**
     * Configures YAML, for example, add implicits.  Default implementation
     * returns a plain builder.  When overriding capture {@code
     * super.newYamlBuilder()} and return that after configuring it.
     */
    protected YamlHelper.Builder newYamlBuilder() {
        return YamlHelper.builder();
    }

    @Override
    protected final YamlGenerateMesseger newMesseger(
            final Class<YamlGenerate> annoType, final Messager messager,
            final Element element) {
        return YamlGenerateMesseger.from(messager, element);
    }

    /**
     * Checks the annotated <var>element</var> for sanity.  Default
     * implementation checks that {@code &#64;YamlGenerate} is on top-level
     * interfaces.  When overriding, return {@code super.preValidate(elemenet)}
     * after yuor own checks.
     *
     * @todo Is the top-level restriction for &#64;YamlGenerate needed?
     */
    @Override
    protected boolean preValidate(final Element element) {
        if (INTERFACE != element.getKind()) {
            out.error("%@ only supported on interfaces");
            return false;
        }

        if (PACKAGE != element.getEnclosingElement().getKind()) {
            out.error("%@ only supports top-level interfaces");
            return false;
        }

        return super.preValidate(element);
    }

    @Override
    protected final String withAnnotationValue() {
        return "template";
    }

    /**
     * Checks the completed processing for sanity.  Default implementation
     * returns {@code true}.  When overriding, return {@code
     * super.postValidate(methods)} after your own checks.
     * <p>
     * Passes in <var>methods</var>, an immutable map of generated full class
     * names to method descriptions in processing order.  Supports
     * post-processing generation of additional sources.
     *
     * @param methods the processed class/methods, never missing
     *
     * @return {@code false} to fail the Javac build
     *
     * @todo Should it pass in enums also?
     */
    @SuppressWarnings("UnusedParameters")
    protected boolean postValidate(
            final Map<String, Map<String, Map<String, Object>>> methods) {
        return true;
    }

    @Override
    protected final boolean postValidate() {
        return super.postValidate() && postValidate(unmodifiableMap(methods));
    }

    @Override
    protected final void process(final Element element,
            final YamlGenerate anno) {
        final String[] inputs = anno.inputs();
        final String namespace = anno.namespace();

        try {
            yaml = newYamlBuilder().build();
            final Resource ftl = loader.getResource(anno.template());
            out = out.withTemplate(ftl);
            template = freemarker.getTemplate(anno.template());

            try {
                final Name packaj = processingEnv.getElementUtils()
                        .getName(namespace);

                for (final String input : inputs)
                    process(element, packaj, input);
            } catch (final Exception e) {
                out.error(e, "%@ cannot process");
            }
        } catch (final Exception e) {
            out.error(e, "%@ cannot create template");
        }
    }

    private enum Generate {
        ENUM, CLASS;

        @Nullable
        private static Generate from(@Nonnull final Object value) {
            if (value instanceof List)
                return ENUM;
            else if (value instanceof Map)
                return CLASS;
            else
                return null;
        }
    }

    private void process(final Element cause, final Name packaj,
            final String input) {
        for (final Loaded loaded : loadAll(input)) {
            out = out.withYaml(loaded.whence);

            for (final Entry<String, Object> each : loaded.yaml.entrySet()) {
                final String key = each.getKey();
                final Object value = each.getValue();
                final Generate generate = Generate.from(value);
                if (null == generate) {
                    out.error(
                            "%@ only supports list (enum) and map (class), not %s in '%s' from '%s' with %s",
                            value.getClass(), key, loaded, value);
                    continue;
                }

                final ZisZuper names = ZisZuper.from(packaj, key);
                if (null == names) {
                    // TODO: Use fail()
                    out.error(
                            "%@ classes have at most one parent for '%s' from '%s'",
                            key, loaded);
                    continue;
                }

                try {
                    switch (generate) {
                    case ENUM:
                        if (null != names.zuper) {
                            fail(names.zis, value, loaded,
                                    "Enums cannot have a parent");
                            continue;
                        }
                        buildEnum(cause, names.zis, cast(value), loaded);
                        break;
                    case CLASS:
                        buildClass(cause, names.zis, names.zuper, cast(value),
                                loaded);
                        break;
                    }
                } catch (final RuntimeException e) {
                    fail(e, names.zis, value, loaded);
                }
            }
        }
    }

    /**
     * Fails the Javac build with details specific to YAML-to-Java code
     * generation after an internal exception.
     *
     * @param e the exception failing the build, never missing
     * @param zis the details for the processed class name, never missing
     * @param value the data details for the class (list for enums, map for
     * classes), never missing
     * @param loaded the loading details for the YAML defining the class,
     * never missing
     */
    protected final void fail(@Nonnull final Exception e,
            @Nonnull final Names zis, @Nonnull final Object value,
            @Nonnull final Loaded loaded) {
        final String type = value instanceof List ? "enum" : "class";
        out.error(e, "%s: Failed building %s '%s' from '%s' with '%s'", e,
                type, zis, loaded, value);
    }

    /**
     * Fails the Javac build with details specific to YAML-to-Java code
     * generation after an internal exception.
     *
     * @param zis the details for the processed class name, never missing
     * @param value the data details for the class (list for enums, map for
     * classes), never missing
     * @param loaded the loading details for the YAML defining the class,
     * never missing
     * @param format a printf-style message, optional
     * @param args arguments for <var>format</var>, if any
     */
    protected final void fail(@Nonnull final Names zis,
            @Nonnull final Object value, @Nonnull final Loaded loaded,
            @Nullable final String format, final Object... args) {
        final String type = value instanceof List ? "enum" : "class";
        final Object[] xArgs = new Object[args.length + 4];
        arraycopy(args, 0, xArgs, 0, args.length);
        xArgs[args.length] = type;
        xArgs[args.length + 1] = zis;
        xArgs[args.length + 2] = loaded;
        xArgs[args.length + 3] = value;
        final String xFormat = null == format
                ? "Failed building %s '%s' from '%s' with '%s'"
                : format + ": Failed building %s '%s' from '%s' with '%s'";
        out.error(xFormat, xArgs);
    }

    /**
     * Builds a FreeMarker model for writing source files.  Essentially the
     * same as {@link Supplier} but throws {@code IOException}.
     */
    @FunctionalInterface
    public interface ModelSupplier {
        @Nonnull
        Map<String, Object> get()
                throws IOException;
    }

    /**
     * Generates source for an {@code enum} from YAML.
     *
     * @param cause the element annotated with {@code &64;YamlGenerate}, never
     * missing
     * @param zis the defails of the processed enum name, never missing
     * @param values the data details for the enum, never missing
     * @param loaded the loading details for the YAML defining the class,
     */
    protected final void buildEnum(@Nonnull final Element cause,
            @Nonnull final Names zis, @Nonnull final List<String> values,
            @Nonnull final Loaded loaded) {
        writeSource(cause, zis, values, loaded,
                () -> modelEnum(zis, values, loaded));
    }

    /**
     * Generates source for a {@code class} from YAML.
     *
     * @param cause the element annotated with {@code &64;YamlGenerate}, never
     * missing
     * @param zis the defails of the processed class name, never missing
     * @param zuper the defails of the processed super class, {@code null} if
     * none
     * @param methods the data details for the class, never missing
     * @param loaded the loading details for the YAML defining the class,
     */
    protected final void buildClass(@Nonnull final Element cause,
            @Nonnull final Names zis, @Nullable final Names zuper,
            @Nonnull final Map<String, Map<String, Object>> methods,
            @Nonnull final Loaded loaded) {
        this.methods.put(zis.fullName, immutable(methods));

        writeSource(cause, zis, methods, loaded,
                () -> modelClass(zis, zuper, methods, loaded));
    }

    private void writeSource(final Element cause, final Names zis,
            final Object values, final Loaded loaded,
            final ModelSupplier model) {
        // "Inside out" to keep exception handling in this method
        try (final Writer writer = new OutputStreamWriter(
                processingEnv.getFiler().createSourceFile(zis.fullName, cause)
                        .openOutputStream())) {
            template.process(model.get(), writer);
        } catch (final IOException e) {
            fail(e, zis, values, loaded);
        } catch (final TemplateException e) {
            template.getSource(e.getColumnNumber(), e.getLineNumber(),
                    e.getEndColumnNumber(), e.getEndLineNumber());
            fail(e, zis, values, loaded);
        }
    }

    private Map<String, Object> modelEnum(final Names zis,
            final List<?> values, final Loaded loaded)
            throws IOException {
        final Map<String, Object> model = commonModel(loaded, zis, null);
        model.put("type", Enum.class.getSimpleName());
        model.put("values", values);
        return model;
    }

    private Map<String, Object> modelClass(final Names zis, final Names zuper,
            final Map<String, Map<String, Object>> rawMethods,
            final Loaded loaded)
            throws IOException {
        final Map<String, Object> model = commonModel(loaded, zis, zuper);
        model.put("type", Class.class.getSimpleName());

        if (null == methods)
            model.put("data", emptyMap());
        else {
            final Map<String, Map<String, Object>> methods
                    = new LinkedHashMap<>(rawMethods.size());
            for (final Entry<String, Map<String, Object>> method : rawMethods
                    .entrySet()) {
                final String name = method.getKey();
                switch (name) {
                case ".meta":
                    // Class details
                    model.put("doc", rawMethods.get(".meta").get("doc"));
                    continue;
                default:
                    // Instance details
                    final Map<String, Object> props = method.getValue();
                    methods.put(name, props);
                    final TypedValue pair = model(name,
                            (String) props.get("type"), props.get("value"));
                    props.put("type", pair.type);
                    props.put("value", pair.value);
                    props.put("override", overridden(zuper, name));
                    props.put("definition", toAnnotationValue(props));
                }
            }
            model.put("methods", methods);
        }
        return model;
    }

    private boolean overridden(final Names zuper, final String method) {
        return null != zuper && methods.get(zuper.fullName)
                .containsKey(method);
    }

    /**
     * @todo Recursive for values which are maps, etc.
     * @todo Handle quotes within quotes
     */
    private static String toAnnotationValue(final Map<String, Object> props) {
        return props.entrySet().stream().
                map(e -> new AbstractMap.SimpleImmutableEntry<>(e.getKey(),
                        Objects.toString(e.getValue()))).
                map(e -> new AbstractMap.SimpleImmutableEntry<>(
                        '"' + e.getKey() + '"', '"' + e.getValue() + '"')).
                flatMap(e -> Stream.of(e.getKey(), e.getValue())).
                collect(joining(", ", "{", "}"));
    }

    @SuppressWarnings("unchecked")
    private static <T> T cast(final Object o) {
        return (T) o;
    }

    private static Map<String, Map<String, Object>> immutable(
            final Map<String, Map<String, Object>> methods) {
        final Map<String, Map<String, Object>> immutable
                = new LinkedHashMap<>();
        methods.entrySet().
                forEach(e -> immutable
                        .put(e.getKey(), unmodifiableMap(e.getValue())));
        return unmodifiableMap(immutable);
    }

    /** @todo Use fail() and return null */
    private static TypedValue model(final String key, final String type,
            final Object value) {
        if (null == value) {
            if (null == type)
                throw new IllegalStateException(
                        format("Missing value and type for '%s", key));
            switch (type) {
            case "int":
                return new TypedValue("int", 0);
            case "double":
                return new TypedValue("double", 0.0d);
            case "list":
                return new TypedValue("list", new ArrayList<>(0));
            case "map":
                return new TypedValue("map", new LinkedHashMap<>(0));
            default:
                return new TypedValue(type, null);
            }
        } else if (null != type)
            // TODO: Actually should be OK for implicit UDT (e.g., Dice)
            throw new IllegalStateException(
                    format("TODO: Both value and type are provided for '%s'",
                            key));
        else if (value instanceof String)
            return new TypedValue("text", value);
        else if (value instanceof Integer)
            return new TypedValue("int", value);
        else if (value instanceof Double)
            return new TypedValue("double", value);
        else if (value instanceof List)
            return new TypedValue("list", value);
        else if (value instanceof Map)
            return new TypedValue("map", value);
        else
            throw new IllegalStateException(
                    format("TODO: Support UDT of '%s' for '%s'",
                            value.getClass().getName(), key));
    }

    private Map<String, Object> commonModel(final Loaded loaded,
            final Names zis, final Names zuper)
            throws IOException {
        return new LinkedHashMap<String, Object>() {{
            put("generator", YamlGenerateProcessor.class.getName());
            put("now", Instant.now().toString());
            put("comments", format("From '%s' using '%s'",
                    loaded.whence.getURI().toString(), template.getName()));
            put("package", zis.packaj);
            put("name", zis.name);
            put("parent", null == zuper ? null : zuper.nameRelativeTo(zis));
        }};
    }

    private List<Loaded> loadAll(final String pattern) {
        final List<Loaded> docs = new ArrayList<>();
        try {
            for (final Resource resource : loader.getResources(pattern))
                try (final InputStream in = resource.getInputStream()) {
                    for (final Object doc : yaml.loadAll(in))
                        docs.add(new Loaded(resource, cast(doc)));
                }
        } catch (final IOException e) {
            out.error(e, "Cannot load");
        }
        return docs;
    }

    public static final class Loaded {
        private final Resource whence;
        private final Map<String, Object> yaml;

        private Loaded(final Resource whence,
                final Map<String, Object> yaml) {
            this.whence = whence;
            this.yaml = yaml;
        }

        @Override
        public String toString() {
            return whence.getDescription() + ": " + yaml;
        }
    }

    private static final class ZisZuper {
        @Nonnull
        private final Names zis;
        @Nullable
        private final Names zuper;

        private static ZisZuper from(final Name packaj, final String key) {
            final String name;
            final String parent;
            final String[] names = space.split(key);
            switch (names.length) {
            case 1:
                name = names[0];
                parent = null;
                break;
            case 2:
                name = names[0];
                parent = names[1];
                break;
            default:
                return null;
            }
            //noinspection ConstantConditions
            return new ZisZuper(Names.from(packaj, name, key),
                    Names.from(packaj, parent, key));
        }

        private ZisZuper(@Nonnull final Names zis,
                @Nullable final Names zuper) {
            this.zis = zis;
            this.zuper = zuper;
        }
    }

    private static final class TypedValue {
        private final String type;
        private final Object value;

        private TypedValue(final String type, final Object value) {
            this.type = type;
            this.value = value;
        }
    }

    private class ResourceTemplateLoader
            extends URLTemplateLoader {
        @Override
        protected URL getURL(final String name) {
            final Resource resource = loader.getResource(name);
            try {
                return resource.exists() ? resource.getURL() : null;
            } catch (final IOException e) {
                out.error(e, "%@ cannot load FTL template from '%s'",
                        resource.getDescription());
                return null;
            }
        }
    }
}
