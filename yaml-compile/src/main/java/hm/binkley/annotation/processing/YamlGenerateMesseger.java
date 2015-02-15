package hm.binkley.annotation.processing;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import hm.binkley.annotation.YamlGenerate;
import hm.binkley.util.YamlHelper;
import org.springframework.core.io.Resource;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import java.util.Map;

import static hm.binkley.util.YamlHelper.Builder.inOneLine;
import static java.lang.System.arraycopy;
import static java.util.Collections.singletonMap;

/**
 * {@code GenerateMessenger} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 * @todo Consider refactoring to subclasses of either block or template
 */
public final class YamlGenerateMesseger
        extends SingleAnnotationMessager<YamlGenerate, YamlGenerateMesseger> {
    private final Resource ftl;
    private final Resource yml;
    // Block and template are exclusive
    private final Map.Entry<String, ? extends Map<String, ?>> block;
    private final Template template;

    static YamlGenerateMesseger from(final Messager messager,
            final Element element) {
        return new YamlGenerateMesseger(messager, element, null, null, null,
                null, null, null);
    }

    private YamlGenerateMesseger(final Messager messager,
            final Element element, final AnnotationMirror mirror,
            final AnnotationValue value, final Resource ftl,
            final Resource yml,
            final Map.Entry<String, ? extends Map<String, ?>> block,
            final Template template) {
        super(YamlGenerate.class, messager, element, mirror, value);
        this.ftl = ftl;
        this.yml = yml;
        this.block = block;
        this.template = template;
    }

    @Override
    public YamlGenerateMesseger withAnnotation(final AnnotationMirror mirror,
            final AnnotationValue value) {
        return new YamlGenerateMesseger(messager, element, mirror, value, ftl,
                yml, block, template);
    }

    public YamlGenerateMesseger withTemplate(final Resource ftl) {
        return new YamlGenerateMesseger(messager, element, mirror, value, ftl,
                yml, block, template);
    }

    public YamlGenerateMesseger withYaml(final Resource yml) {
        return new YamlGenerateMesseger(messager, element, mirror, value, ftl,
                yml, block, template);
    }

    public YamlGenerateMesseger atYamlBlock(
            final Map.Entry<String, ? extends Map<String, ?>> block) {
        return new YamlGenerateMesseger(messager, element, mirror, value, ftl,
                yml, block, template);
    }

    public YamlGenerateMesseger clearYamlBlock() {
        return new YamlGenerateMesseger(messager, element, mirror, value, ftl,
                yml, null, template);
    }

    public YamlGenerateMesseger atTemplateSource(final Template template) {
        return new YamlGenerateMesseger(messager, element, mirror, value, ftl,
                yml, block, template);
    }

    @Override
    protected MessageArgs messageArgs(final Exception cause,
            final String format, final Object... args) {
        final String xFormat;
        final Object[] xArgs;

        if (null == ftl && null == yml) {
            xFormat = format;
            xArgs = args;
        } else if (null == yml) {
            xFormat = "(%s): " + format;
            xArgs = new Object[1 + args.length];
            xArgs[0] = ftl.getDescription();
            arraycopy(args, 0, xArgs, 1, args.length);
        } else if (null == ftl) {
            throw new AssertionError("FTL cannot be null if YML is present");
        } else if (null != block) {
            xFormat = "%s(%s): " + format + " with block: %s";
            xArgs = new Object[3 + args.length];
            xArgs[0] = yml.getDescription();
            xArgs[1] = ftl.getDescription();
            arraycopy(args, 0, xArgs, 2, args.length);
            final String yaml = YamlHelper.builder().
                    build(inOneLine()).
                    dump(singletonMap(block.getKey(), block.getValue()));
            // Chop trailing newline
            xArgs[xArgs.length - 1] = yaml.substring(0, yaml.length() - 2);
        } else if (null != template && cause instanceof TemplateException) {
            xFormat = "%s(%s): " + format + " at template source: %s";
            xArgs = new Object[3 + args.length];
            xArgs[0] = yml.getDescription();
            xArgs[1] = ftl.getDescription();
            arraycopy(args, 0, xArgs, 2, args.length);
            final TemplateException e = (TemplateException) cause;
            xArgs[xArgs.length - 1] = template
                    .getSource(e.getColumnNumber(), e.getLineNumber(),
                            e.getEndColumnNumber(), e.getEndLineNumber());
        } else {
            xFormat = "%s(%s): " + format;
            xArgs = new Object[2 + args.length];
            xArgs[0] = yml.getDescription();
            xArgs[1] = ftl.getDescription();
            arraycopy(args, 0, xArgs, 2, args.length);
        }

        return super.messageArgs(cause, xFormat, xArgs);
    }
}
