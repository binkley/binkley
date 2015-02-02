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
import hm.binkley.util.YamlHelper.Builder;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.DumperOptions.LineBreak;
import org.yaml.snakeyaml.DumperOptions.ScalarStyle;
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
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static freemarker.template.Configuration.VERSION_2_3_21;
import static freemarker.template.TemplateExceptionHandler.DEBUG_HANDLER;
import static hm.binkley.annotation.processing.MethodDescription.methodDescription;
import static java.lang.String.format;
import static java.lang.System.arraycopy;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
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
    private final List<String> roots = findRootsOf(getClass());
    private final Configuration freemarker;
    private Yaml yaml;
    private LoadedTemplate template;

    // In context, system class loader does not have maven class path
    protected final ResourcePatternResolver loader
            = new PathMatchingResourcePatternResolver(
            getClass().getClassLoader());

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
     * returns <var>builder</var>.
     *
     * @param builder the YAML builder
     */
    protected YamlHelper.Builder setup(final Builder builder) {
        return builder;
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
    protected boolean preValidate(final Element element,
            final YamlGenerate anno) {
        if (INTERFACE != element.getKind()) {
            out.error("%@ only supported on interfaces");
            return false;
        }

        if (PACKAGE != element.getEnclosingElement().getKind()) {
            out.error("%@ only supports top-level interfaces");
            return false;
        }

        return super.preValidate(element, anno);
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
            final YamlGenerate anno,
            final Map<String, Map<String, Map<String, Object>>> methods) {
        return true;
    }

    @Override
    protected final boolean postValidate(final Element element,
            final YamlGenerate anno) {
        return postValidate(element, anno, unmodifiableMap(methods)) && super
                .postValidate(element, anno);
    }

    @Override
    protected final void process(final Element element,
            final YamlGenerate anno) {
        final String[] inputs = anno.inputs();
        final String namespace = anno.namespace();

        try {
            yaml = setup(YamlHelper.builder()).build();
            withTemplate(anno.template());

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

    /**
     * Uses the Freemarker template found at <var>path</var>.
     *
     * @param path the template path, never missing
     *
     * @throws IOException if the template cannot be loaded
     */
    protected void withTemplate(@Nonnull final String path)
            throws IOException {
        template = loadTemplate(path);
        out = out.withTemplate(template.whence);
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

    /**
     * Builds a Java class from the given parameters using
     * <var>description</var> as the YAML source.  Used for classes generated
     * programmatically rather than from YAML.
     *
     * @param cause the element annotated with {@link YamlGenerate}, never
     * missing
     * @param names the class and superclass names, never missing
     * @param methods the methods, never missing
     * @param format the programmatic source description, never missing
     * @param args any formatting arguments for <var>format</var>
     */
    protected final void build(@Nonnull final Element cause,
            @Nonnull final ZisZuper names,
            @Nonnull final Map<String, Map<String, Object>> methods,
            @Nonnull final String format, final Object... args) {
        build(cause, names, methods, new UnLoaded(format, args));
    }

    /**
     * Builds a Java class from the given parameters using <var>loaded</var>
     * as the YAML source.  Used for classes generated from YAML files.
     *
     * @param cause the element annotated with {@link YamlGenerate}, never
     * missing
     * @param names the class and superclass names, never missing
     * @param methods the methods, never missing
     * @param loaded the loaded YAML source, never missing
     */
    protected final void build(@Nonnull final Element cause,
            @Nonnull final ZisZuper names,
            @Nonnull final Map<String, Map<String, Object>> methods,
            @Nonnull final Loaded<?> loaded) {
        try (final Writer writer = new OutputStreamWriter(
                processingEnv.getFiler()
                        .createSourceFile(names.zis.fullName, cause)
                        .openOutputStream())) {
            template.what.process(model(names, methods, loaded), writer);
        } catch (final IOException e) {
            fail(e, names.zis, methods, loaded);
        } catch (final TemplateException e) {
            template.what.getSource(e.getColumnNumber(), e.getLineNumber(),
                    e.getEndColumnNumber(), e.getEndLineNumber());
            fail(e, names.zis, methods, loaded);
        }
    }

    private Map<String, Object> model(@Nonnull final ZisZuper names,
            @Nullable final Map<String, Map<String, Object>> values,
            @Nonnull final Loaded<?> loaded) {
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
            final Map<String, Object> block = block(names, method);

            final List<String> definition = toAnnotationValue(block);
            switch (name) {
            case ".meta":
                // Class details
                model.put("definition", definition);
                model.put("doc", block.get("doc"));
                break;
            default:
                block.put("definition", definition);
                generate.updateModel(name, model, block, names, methods);
                loops.put(name, block);
                break;
            }
        }

        return model;
    }

    /**
     * Strategy: <ol><li>If there are values, use them.</li> <li>If this is
     * ".meta" and there is a supertype, use the supertype values.</li>
     * <li>Use empty values.</li></ol>
     */
    private Map<String, Object> block(final ZisZuper names,
            final Entry<String, Map<String, Object>> method) {
        final Map<String, Object> values = method.getValue();
        if (null != values)
            // Clone to leave original YAML alone
            return new LinkedHashMap<>(values);
        final String name = method.getKey();
        if (null != names.zuper && ".meta".equals(name)) {
            methods.get(names.zuper.fullName).get(name);
            return new LinkedHashMap<>();
        }
        return new LinkedHashMap<>();
    }

    private List<String> toAnnotationValue(final Map<String, Object> props) {
        return props.entrySet().stream().
                map(e -> singletonMap(e.getKey(), e.getValue())).
                map(yaml::dump).
                map(StringEscapeUtils::escapeJava).
                map(e -> format("\"%s\"", e)).
                collect(toList());
    }

    /** @todo Utility code */
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

    /**
     * @todo Shared with MethodDescription - bad placement
     * @todo Is this in SnakeYAML?
     */
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

    private Map<String, Object> commonModel(@Nonnull final ZisZuper names,
            @Nonnull final Loaded<?> loaded) {
        final LinkedHashMap<String, Object> model = new LinkedHashMap<>();
        model.put("generator", getClass().getName());
        model.put("now", Instant.now().toString());
        model.put("comments",
                format("From '%s' using '%s'", loaded.where(), template));
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
                        docs.add(new LoadedYaml(pattern, resource, cast(doc),
                                roots));
                }
        } catch (final IOException e) {
            out.error(e, "Cannot load");
        }
        return docs;
    }

    private static List<String> findRootsOf(
            final Class<? extends YamlGenerateProcessor> relativeTo) {
        try {
            return ClassPath.from(relativeTo.getClassLoader()).
                    getResources().stream().
                    map(ResourceInfo::getResourceName).
                    collect(toList());
        } catch (final IOException e) {
            throw new IOError(e);
        }
    }

    protected LoadedTemplate loadTemplate(final String path)
            throws IOException {
        final Resource ftl = loader.getResource(path);
        return new LoadedTemplate(path, ftl,
                freemarker.getTemplate(ftl.getURI().toString()));
    }

    protected static final class LoadedTemplate
            extends Loaded<Template> {
        private LoadedTemplate(final String path, final Resource whence,
                final Template template) {
            super(path, whence, template);
        }

        @Override
        public String where() {
            return where;
        }

        @Override
        public String toString() {
            final String whence = this.whence.getDescription();
            final String where = where();
            return whence.equals(where) ? whence
                    : format("%s(%s)", whence, where);
        }
    }

    protected final class UnLoaded
            extends Loaded<Void> {
        public UnLoaded(final String format, final Object... args) {
            super(out.annoFormat(format, args), null, null);
        }

        @Override
        public String where() {
            return where;
        }

        @Override
        public String toString() {
            return where();
        }
    }

    protected static final class LoadedYaml
            extends Loaded<Map<String, Object>> {
        public final String path;

        @Override
        public String where() {
            return path;
        }

        private LoadedYaml(final String pathPattern, final Resource whence,
                final Map<String, Object> yaml, final List<String> roots)
                throws IOException {
            super(pathPattern, whence, yaml);
            path = path(pathPattern, whence, roots);
        }

        @Override
        public String toString() {
            final String whence = this.whence.getDescription();
            return whence.equals(where) ? format("%s: %s", whence, what)
                    : format("%s(%s): %s", whence, where, what);
        }
    }

    private enum Generate {
        ENUM("Enum", "values") {
            @Override
            protected void updateModel(final String name,
                    final Map<String, Object> model,
                    final Map<String, Object> block, final ZisZuper names,
                    final Map<String, Map<String, Map<String, Object>>> methods) {
                // Do nothing
            }
        },
        CLASS("Class", "methods") {
            @Override
            protected void updateModel(final String name,
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

        protected abstract void updateModel(final String name,
                final Map<String, Object> model,
                final Map<String, Object> block, final ZisZuper names,
                final Map<String, Map<String, Map<String, Object>>> methods);

        @Nonnull
        private static Generate from(@Nonnull final ZisZuper names) {
            final Names zuper = names.zuper;
            return null == zuper || !"Enum".equals(zuper.name) ? CLASS : ENUM;
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

    public static void main(final String... args) {
        final Map<String, Object> values
                = new LinkedHashMap<String, Object>() {{
            put("a", "aaa");
            put("b", 2);
            put("c", true);
            put("d", 3.14159d);
            put("e", "This is\ta \\gnarly \"string\"!");
            put("f", asList("a", true, 3));
            put("g", new LinkedHashMap<String, Object>() {{
                put("p", "ppp");
                put("q", 3);
                put("r", true);
            }});
        }};
        final Yaml yaml = YamlHelper.builder().
                dumper(d -> {
                    d.setPrettyFlow(true);
                    d.setWidth(Integer.MAX_VALUE);
                    d.setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED);
                    d.setDefaultFlowStyle(FlowStyle.FLOW);
                    d.setLineBreak(LineBreak.UNIX);
                    d.setTimeZone(null);
                }).
                build();

        values.entrySet().stream().
                map(e -> singletonMap(e.getKey(), e.getValue())).
                map(yaml::dump).
                // map(e -> e.substring(0, e.length() - 1)). // chop nl
                map(StringEscapeUtils::escapeJava).
                map(e -> format("\"%s\"", e)).
                forEach(System.out::println);
    }
}
