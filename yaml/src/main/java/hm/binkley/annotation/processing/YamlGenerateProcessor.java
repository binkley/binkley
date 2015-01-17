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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import static freemarker.template.Configuration.VERSION_2_3_21;
import static freemarker.template.TemplateExceptionHandler.DEBUG_HANDLER;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.regex.Pattern.compile;
import static javax.lang.model.SourceVersion.RELEASE_8;
import static javax.lang.model.element.ElementKind.INTERFACE;
import static javax.lang.model.element.ElementKind.PACKAGE;

/**
 * {@code YamlGenerateProcessor} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 * @todo Parse "freemarker.version" from Maven to construct latest Version
 * @todo Remember class methods to define a base layer defining all
 * @todo Docstrings on enum values
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

    /** Configures YAML, for example, add implicits. */
    protected YamlHelper.Builder setupYaml(final YamlHelper.Builder builder) {
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
            yaml = setupYaml(YamlHelper.builder()).build();
            final Resource ftl = loader.getResource(anno.template());
            out = out.withTemplate(ftl);
            template = freemarker.getTemplate(anno.template());

            try {
                final Name packaj = processingEnv.getElementUtils()
                        .getName(namespace);

                for (final String input : inputs)
                    process(element, packaj, input);
            } catch (final Exception e) {
                out.error(e, "Cannot process");
            }
        } catch (final Exception e) {
            out.error(e, "Cannot create template");
        }
    }

    private void process(final Element cause, final Name packaj,
            final String input) {
        for (final Loaded loaded : loadAll(input)) {
            out = out.withYaml(loaded.whence);

            for (final Entry<String, Object> each : loaded.doc.entrySet()) {
                final String key = each.getKey();
                final NameParent nameParent = NameParent.from(key);
                if (null == nameParent) {
                    out.error(
                            "Classes have at most one parent for '%s' in %s",
                            key, loaded);
                    continue;
                }

                final Names zis = Names.from(packaj, nameParent.name);
                final Names zuper = Names.from(packaj, nameParent.parent);
                final Object value = each.getValue();
                final boolean isEnum = value instanceof List;

                try {
                    if (isEnum) {
                        if (null != zuper) {
                            out.error(
                                    "Enums cannot have parent for '%s' in %s",
                                    key, loaded);
                            return;
                        }
                        buildEnum(cause, loaded.whence, zis, cast(value),
                                loaded);
                    } else if (value instanceof Map)
                        buildClass(cause, loaded.whence, zis, zuper,
                                cast(value), loaded);
                    else
                        out.error(
                                "%@ only supports list (enum) and map (class), not (%s) %s in %s",
                                value.getClass(), value, loaded);
                } catch (final Exception e) {
                    fail(e, zis, value, loaded);
                }
            }
        }
    }

    protected final void fail(final Exception e, final Names zis,
            final Object value, final Loaded loaded) {
        final boolean isEnum = value instanceof List;
        out.error(e, "Failed building %s '%s' with '%s' in %s",
                isEnum ? "enum" : "class", zis.fullName, value, loaded);
    }

    protected final void buildEnum(final Element cause, final Resource whence,
            final Names zis, final List<String> values, final Loaded loaded) {
        try (final Writer writer = new OutputStreamWriter(
                processingEnv.getFiler().createSourceFile(zis.fullName, cause)
                        .openOutputStream())) {
            template.process(
                    modelEnum(template.getName(), whence.getURI().toString(),
                            zis, values), writer);
        } catch (final IOException | TemplateException e) {
            fail(e, zis, values, loaded);
        }
    }

    private static Map<String, Object> modelEnum(final String ftl,
            final String yml, final Names zis, final List<?> values) {
        final Map<String, Object> model = commonModel(ftl, yml, zis, null);
        model.put("type", Enum.class.getSimpleName());
        model.put("values", values);
        return model;
    }

    protected final void buildClass(final Element cause,
            final Resource whence, final Names zis, final Names zuper,
            final Map<String, Map<String, Object>> methods,
            final Loaded loaded) {
        this.methods.put(zis.fullName, immutable(methods));

        try (final Writer writer = new OutputStreamWriter(
                processingEnv.getFiler().
                        createSourceFile(zis.fullName, cause).
                        openOutputStream())) {
            template.process(
                    modelClass(template.getName(), whence.getURI().toString(),
                            zis, zuper, methods), writer);
        } catch (final IOException | TemplateException e) {
            fail(e, zis, methods, loaded);
        }
    }

    /** @todo Yucky code */
    private Map<String, Object> modelClass(final String ftl, final String yml,
            final Names zis, final Names zuper,
            final Map<String, Map<String, Object>> methods) {
        final Map<String, Object> model = commonModel(ftl, yml, zis, zuper);
        model.put("type", Class.class.getSimpleName());
        if (null == methods)
            model.put("data", emptyMap());
        else {
            for (final Entry<String, Map<String, Object>> method : methods
                    .entrySet()) {
                final String name = method.getKey();
                final Map<String, Object> props = method.getValue();
                final TypedValue pair = model(name,
                        (String) props.get("type"), props.get("value"));
                props.put("type", pair.type);
                props.put("value", pair.value);
                props.put("override", overridden(zuper, name));
            }
            model.put("methods", methods);
        }
        return model;
    }

    private boolean overridden(final Names zuper, final String method) {
        return null != zuper && methods.get(zuper.fullName)
                .containsKey(method);
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

    private static Map<String, Object> commonModel(final String ftl,
            final String yml, final Names zis, final Names zuper) {
        return new HashMap<String, Object>() {{
            put("generator", YamlGenerateProcessor.class.getName());
            put("now", Instant.now().toString());
            put("comments", format("From '%s' using '%s'", yml, ftl));
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

    private static final class NameParent {
        private final String name;
        private final String parent;

        private NameParent(final String name, final String parent) {
            this.name = name;
            this.parent = parent;
        }

        private static NameParent from(final String key) {
            final String[] names = space.split(key);
            switch (names.length) {
            case 1:
                return new NameParent(names[0], null);
            case 2:
                return new NameParent(names[0], names[1]);
            default:
                return null;
            }
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

    private static final class Loaded {
        private final Resource whence;
        private final Map<String, Object> doc;

        private Loaded(final Resource whence, final Map<String, Object> doc) {
            this.whence = whence;
            this.doc = doc;
        }

        @Override
        public String toString() {
            return whence.getDescription() + ": " + doc;
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
                out.error(e, "Cannot load FTL template from '%s'",
                        resource.getDescription());
                return null;
            }
        }
    }
}
