package hm.binkley.annotation.processing;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ResourceInfo;
import freemarker.cache.URLTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import hm.binkley.annotation.YamlGenerate;
import hm.binkley.util.YamlHelper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.processing.Messager;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Stream;

import static freemarker.template.Configuration.VERSION_2_3_21;
import static freemarker.template.TemplateExceptionHandler.DEBUG_HANDLER;
import static hm.binkley.annotation.processing.MethodDescription.methodDescription;
import static java.lang.String.format;
import static java.lang.System.arraycopy;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.SourceVersion.RELEASE_8;
import static javax.lang.model.element.ElementKind.INTERFACE;
import static javax.lang.model.element.ElementKind.PACKAGE;

/**
 * {@code YamlGenerateProcessor} generates Java source enums and classes from
 * YAML descriptions.
 * <p>
 * Type names are those <a href="http://yaml.org/type/">defined by YAML
 * 1.1</a>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 * @todo Much better, less hacky APi for extension
 * @todo Parse "freemarker.version" from Maven to construct latest Version
 * @todo Custom configuration of Freemarker
 * @todo Errors in blocks should show source specific to block, not whole
 */
@SupportedAnnotationTypes("hm.binkley.annotation.YamlGenerate")
@SupportedSourceVersion(RELEASE_8)
public class YamlGenerateProcessor
        extends
        SingleAnnotationProcessor<YamlGenerate, YamlGenerateMesseger> {
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
    protected boolean postValidate(final Element element,
            final Map<String, Map<String, Map<String, Object>>> methods) {
        return true;
    }

    @Override
    protected final boolean postValidate(final Element element) {
        return postValidate(element, unmodifiableMap(methods)) && super
                .postValidate(element);
    }

    protected LoadedTemplate loadTemplate(final String path)
            throws IOException {
        final Resource ftl = loader.getResource(path);
        return new LoadedTemplate(ftl,
                freemarker.getTemplate(ftl.getURI().toString()), path);
    }

    @Override
    protected final void process(final Element element,
            final YamlGenerate anno) {
        final String[] inputs = anno.inputs();
        final String namespace = anno.namespace();

        try {
            yaml = newYamlBuilder().build();
            final LoadedTemplate loadedTemplate = loadTemplate(
                    anno.template());
            template = loadedTemplate.what;
            out = out.withTemplate(loadedTemplate.whence);

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
        ENUM("Enum", "values") {
            @Override
            protected void generate(final String name,
                    final Map<String, Object> model,
                    final Map<String, Object> block, final ZisZuper names,
                    final Map<String, Map<String, Map<String, Object>>> methods) {
                // Do nothing
            }
        }, CLASS("Class", "methods") {
            @Override
            protected void generate(final String name,
                    final Map<String, Object> model,
                    final Map<String, Object> block, final ZisZuper names,
                    final Map<String, Map<String, Map<String, Object>>> methods) {
                model.put("parent", names.parent());
                final MethodDescription method = methodDescription(name,
                        (String) block.get("type"), block.get("value"));
                block.put("name", method.name);
                block.put("type", method.type);
                if ("seq".equals(method.type)) {
                    final List<?> elements = cast(method.value);
                    final List<Map<String, ?>> value = new ArrayList<>(
                            elements.size());
                    elements.forEach(v -> value.add(ImmutableMap
                            .of("value", v, "type", typeOf(v))));
                    block.put("value", value);
                } else if ("pairs".equals(method.type)) {
                    final Map<String, ?> elements = cast(method.value);
                    final Map<String, Map<String, ?>> value
                            = new LinkedHashMap<>(elements.size());
                    elements.forEach((k, v) -> value.put(k,
                            ImmutableMap.of("value", v, "type", typeOf(v))));
                    block.put("value", value);
                } else
                    block.put("value", method.value);
                block.put("override", names.overridden(methods, name));
            }
        };

        private final String typeKey;
        private final String loopKey;

        Generate(final String typeKey, final String loopKey) {
            this.typeKey = typeKey;
            this.loopKey = loopKey;
        }

        protected abstract void generate(final String name,
                final Map<String, Object> model,
                final Map<String, Object> block, final ZisZuper names,
                final Map<String, Map<String, Map<String, Object>>> methods);

        @Nonnull
        private static Generate from(@Nonnull final ZisZuper names) {
            final Names zuper = names.zuper;
            return null == zuper || !"Enum".equals(zuper.name) ? CLASS : ENUM;
        }
    }

    private void process(final Element cause, final Name packaj,
            final String input) {
        for (final LoadedYaml loaded : loadAll(input)) {
            out = out.withYaml(loaded.whence);

            for (final Entry<String, Object> each : loaded.what.entrySet()) {
                final String key = each.getKey();

                final ZisZuper names = ZisZuper.from(packaj, key);
                if (null == names) {
                    // TODO: Use fail()
                    out.error(
                            "%@ classes have at most one parent for '%s' from '%s'",
                            key, loaded);
                    continue;
                }

                final Map<String, Map<String, Object>> values = cast(
                        each.getValue());
                try {
                    build(cause, names, values, loaded);
                } catch (final RuntimeException e) {
                    fail(e, names.zis, values, loaded);
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
            @Nullable final Loaded<?> loaded) {
        final String type = value instanceof List ? "enum" : "class";
        out.error(e, "%s: Failed building %s '%s' from '%s' with '%s'", e,
                type, zis, null == loaded ? "Did not load" : loaded, value);
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
            @Nonnull final Object value, @Nullable final Loaded<?> loaded,
            @Nullable final String format, final Object... args) {
        final String type = value instanceof List ? "enum" : "class";
        final Object[] xArgs = new Object[args.length + 4];
        arraycopy(args, 0, xArgs, 0, args.length);
        xArgs[args.length] = type;
        xArgs[args.length + 1] = zis;
        xArgs[args.length + 2] = null == loaded ? "Did not load" : loaded;
        xArgs[args.length + 3] = value;
        final String xFormat = null == format
                ? "Failed building %s '%s' from '%s' with '%s'"
                : format + ": Failed building %s '%s' from '%s' with '%s'";
        out.error(xFormat, xArgs);
    }

    protected final void build(@Nonnull final Element cause,
            @Nonnull final ZisZuper names,
            @Nonnull final Map<String, Map<String, Object>> methods,
            @Nonnull final Loaded<?> loaded) {
        try (final Writer writer = new OutputStreamWriter(
                processingEnv.getFiler()
                        .createSourceFile(names.zis.fullName, cause)
                        .openOutputStream())) {
            template.process(model(names, methods, loaded), writer);
        } catch (final IOException e) {
            fail(e, names.zis, methods, loaded);
        } catch (final TemplateException e) {
            template.getSource(e.getColumnNumber(), e.getLineNumber(),
                    e.getEndColumnNumber(), e.getEndLineNumber());
            fail(e, names.zis, methods, loaded);
        }
    }

    private Map<String, Object> model(final ZisZuper names,
            final Map<String, Map<String, Object>> values,
            final Loaded<?> loaded) {
        final Generate generate = Generate.from(names);
        if (Generate.CLASS == generate)
            methods.put(names.zis.fullName, immutable(values));

        final Map<String, Object> model = commonModel(names, loaded);
        model.put("type", generate.typeKey);

        final String loopKey = generate.loopKey;
        if (null == values) {
            model.put(loopKey, emptyMap());
            return model;
        }

        final Map<String, Map<String, Object>> loops = new LinkedHashMap<>(
                values.size());
        model.put(loopKey, loops);
        model.put("definition", toAnnotationValue(emptyMap()));

        for (final Entry<String, Map<String, Object>> method : values
                .entrySet()) {
            final String name = method.getKey();
            final Map<String, Object> value = method.getValue();
            final Map<String, Object> block = null == value
                    ? new LinkedHashMap<>() : value;

            final List<String> definition = toAnnotationValue(block);
            switch (name) {
            case ".meta":
                // Class details
                model.put("definition", definition);
                model.put("doc", block.get("doc"));
                break;
            default:
                block.put("definition", definition);
                generate.generate(name, model, block, names, methods);
                loops.put(name, block);
                break;
            }
        }

        return model;
    }

    /**
     * @todo Recursive for values which are maps, etc.
     * @todo Handle quotes within quotes
     */
    private static List<String> toAnnotationValue(
            final Map<String, Object> props) {
        return props.entrySet().stream().
                map(e -> {
                    final Object value = e.getValue();
                    return new AbstractMap.SimpleImmutableEntry<>(
                            e.getKey() + ":" + (null == value ? "null"
                                    : typeOf(value)), value);
                }).
                map(e -> new AbstractMap.SimpleImmutableEntry<>(e.getKey(),
                        Objects.toString(e.getValue()))).
                map(e -> new AbstractMap.SimpleImmutableEntry<>(
                        '"' + e.getKey() + '"', '"' + e.getValue() + '"')).
                flatMap(e -> Stream.of(e.getKey(), e.getValue())).
                collect(toList());
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

    /** @todo Shared with MethodDescription - bad placement */
    public static String typeOf(final Object value) {
        if (value instanceof String)
            return "str";
        else if (value instanceof Boolean)
            return "bool";
        else if (value instanceof Integer)
            return "int";
        else if (value instanceof Double)
            return "float";
        else if (value instanceof List)
            return "seq";
        else if (value instanceof Map)
            return "pairs";
        else
            return value.getClass().getName();
    }

    private Map<String, Object> commonModel(final ZisZuper names,
            final Loaded<?> loaded) {
        final LinkedHashMap<String, Object> model = new LinkedHashMap<>();
        model.put("generator", YamlGenerateProcessor.class.getName());
        model.put("now", Instant.now().toString());
        model.put("comments", format("From '%s' using '%s'", loaded.where(),
                template.getName()));
        model.put("package", names.zis.packaj);
        model.put("name", names.zis.name);
        return model;
    }

    private List<LoadedYaml> loadAll(final String pattern) {
        final List<LoadedYaml> docs = new ArrayList<>();
        try {
            for (final Resource resource : loader.getResources(pattern))
                try (final InputStream in = resource.getInputStream()) {
                    for (final Object doc : yaml.loadAll(in))
                        docs.add(
                                new LoadedYaml(pattern, resource, cast(doc)));
                }
        } catch (final IOException e) {
            out.error(e, "Cannot load");
        }
        return docs;
    }

    protected static final class LoadedTemplate
            extends Loaded<Template> {
        private LoadedTemplate(final Resource whence, final Template template,
                final String path) {
            super(path, whence, template);
        }

        @Override
        public String where() {
            return where;
        }

        @Override
        public String toString() {
            return format("%s(%s): %s", whence.getDescription(), where(),
                    what.getName());
        }
    }

    protected static final class LoadedYaml
            extends Loaded<Map<String, Object>> {
        private static final List<String> roots;
        public final String path;

        static {
            try {
                roots = ClassPath
                        .from(YamlGenerateProcessor.class.getClassLoader()).
                                getResources().stream().
                                map(ResourceInfo::getResourceName).
                                collect(toList());
            } catch (final IOException e) {
                throw new IOError(e);
            }
        }

        @Override
        public String where() {
            return path;
        }

        private LoadedYaml(final String pattern, final Resource whence,
                final Map<String, Object> yaml)
                throws IOException {
            super(pattern, whence, yaml);
            path = path(pattern, whence);
        }

        private static String path(final String pattern,
                final Resource whence)
                throws IOException {
            if (whence instanceof ClassPathResource)
                return format("classpath:/%s(%s)",
                        ((ClassPathResource) whence).getPath(), pattern);
            else if (whence instanceof FileSystemResource)
                return format("%s(%s)", shorten(whence.getURI()), pattern);
            else
                return format("%s(%s)", whence.getURI().toString(), pattern);
        }

        private static String shorten(final URI uri) {
            final String path = uri.getPath();
            return roots.stream().
                    filter(path::endsWith).
                    map(root -> "classpath:/" + root).
                    findFirst().
                    orElse(uri.toString());
        }

        @Override
        public String toString() {
            return format("%s(%s): %s", whence.getDescription(), where, what);
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
