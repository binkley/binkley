package hm.binkley.annotation.processing;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ResourceInfo;
import freemarker.cache.URLTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import hm.binkley.annotation.YamlGenerate;
import hm.binkley.annotation.processing.y.YModel;
import hm.binkley.annotation.processing.y.YType;
import hm.binkley.util.YamlHelper;
import hm.binkley.util.YamlHelper.Builder;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static freemarker.template.Configuration.VERSION_2_3_21;
import static freemarker.template.TemplateExceptionHandler.DEBUG_HANDLER;
import static hm.binkley.annotation.processing.Utils.cast;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.SourceVersion.RELEASE_8;
import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.INTERFACE;

/**
 * {@code YamlGenerateProcessor} generates Java source enums and classes from
 * YAML descriptions.
 * <p>
 * Type names are those <a href="http://yaml.org/type/">defined by YAML
 * 1.1</a>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 * @todo Parse "freemarker.version" from Maven to construct latest Version
 * @todo Errors in blocks should show source specific to block, not whole
 * @todo &64;Override for non-YAML base class/interface
 */
@SupportedAnnotationTypes("hm.binkley.annotation.YamlGenerate")
@SupportedSourceVersion(RELEASE_8)
public class YamlGenerateProcessor
        extends
        SingleAnnotationProcessor<YamlGenerate, YamlGenerateMesseger> {

    // TODO: BROKEN, passed to subclass, never acutally populated
    private final Map<String, Map<String, Map<String, Object>>> methods
            = new LinkedHashMap<>();
    private final List<String> roots = findRootsOf(getClass());
    private final Configuration freemarker;
    private final Yaml yaml;
    private LoadedTemplate template;

    // In context, system class loader does not have maven class path
    protected final ResourcePatternResolver loader
            = new PathMatchingResourcePatternResolver(
            getClass().getClassLoader());

    public YamlGenerateProcessor() {
        super(YamlGenerate.class);
        final Configuration freemarker = new Configuration(VERSION_2_3_21);
        freemarker.setTemplateLoader(new ResourceTemplateLoader());
        freemarker.setDefaultEncoding(UTF_8.name());
        // Dump stacktrace as only called during compilation, not runtime
        freemarker.setTemplateExceptionHandler(DEBUG_HANDLER);
        this.freemarker = configure(freemarker);
        yaml = configure(YamlHelper.builder()).build();
    }

    /**
     * Configures YAML before processing, for example, to add implicits.
     * Default implementation returns <var>builder</var>, already configured
     * for standard use.
     *
     * @param builder the YAML builder, never missing
     *
     * @return the YAML builder, never missing
     */
    @Nonnull
    protected YamlHelper.Builder configure(@Nonnull final Builder builder) {
        return builder;
    }

    /**
     * Configures Freemarker before processing, for example, to customize
     * finding templates. Default implementation return <var>freemarker</var>,
     * already configured for standard use.
     *
     * @param freemarker the Freemarker configuration, never missing
     *
     * @return the Freemarker configuration, never missing
     */
    @Nonnull
    protected Configuration configure(
            @Nonnull final Configuration freemarker) {
        return freemarker;
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
     * after your own checks.
     *
     * @todo Revisit requiring top-level elements
     * @todo Have generated classes implement/extend interface/class
     */
    @Override
    protected boolean preValidate(final Element element,
            final YamlGenerate anno) {
        out.note("Generating Java from YAML for '%s' with %@", element);

        final boolean valid = super.preValidate(element, anno);
        if (!valid)
            return false;

        if (!asList(INTERFACE, CLASS).contains(element.getKind())) {
            out.error("%@ only supported on interfaces and classes");
            return false;
        }

        return true;
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
    protected final void process(final Element root,
            final YamlGenerate anno) {
        final String[] inputs = anno.inputs();
        final String namespace = anno.namespace();

        try {
            withTemplate(anno.template());

            try {
                final Name packaj = processingEnv.getElementUtils()
                        .getName(namespace);

                for (final String input : inputs)
                    process(root, packaj, input);
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
        out = out.
                withTemplate(template.whence).
                atTemplateSource(template.what);
    }

    private void process(final Element root, final Name packaj,
            final String input) {
        LOAD:
        for (final LoadedYaml loaded : loadAll(input)) {
            out = out.withYaml(loaded.whence);

            for (final YType type : new YModel(root, template, loaded, packaj,
                    setter -> out = setter.apply(out)).list())
                try {
                    build(root, type, loaded);
                } catch (final RuntimeException e) {
                    out.error(e, "Failed building '%s' from '%s'", e, type,
                            loaded);
                    continue LOAD;
                }

            out = out.clearYamlBlock();
        }
    }

    /**
     * Fails the Javac build with details specific to YAML-to-Java code
     * generation after an internal exception.
     *
     * @param e the exception failing the build, never missing
     * @param names the details for the processed class name, never missing
     * @param value the data details for the class (list for enums, map for
     * classes), never missing
     * @param loaded the loading details for the YAML defining the class,
     * never missing
     */
    protected final void fail(@Nonnull final Exception e,
            @Nonnull final Names names, @Nonnull final Object value,
            @Nullable final Loaded<?> loaded) {
        final String type = value instanceof List ? "enum" : "class";
        out.error(e, "%s: Failed building %s '%s' from '%s' with '%s'", e,
                type, names, null == loaded ? "Did not load" : loaded, value);
    }

    protected final void build(@Nonnull final Element root,
            @Nonnull final YType type, @Nonnull final LoadedYaml loaded) {
        try (final Writer writer = new OutputStreamWriter(
                processingEnv.getFiler().
                        createSourceFile(type.names.zis.fullName, root).
                        openOutputStream())) {
            final Map<String, ?> x = type.asMap();
            System.err.println("XXX = " + x);
            template.what.process(x, writer);
        } catch (final IOException | TemplateException e) {
            fail(e, type.names.zis, type, loaded);
        }
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
