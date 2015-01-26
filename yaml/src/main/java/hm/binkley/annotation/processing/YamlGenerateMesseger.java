package hm.binkley.annotation.processing;

import hm.binkley.annotation.YamlGenerate;
import org.springframework.core.io.Resource;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;

import static java.lang.System.arraycopy;

/**
 * {@code GenerateMessenger} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public final class YamlGenerateMesseger
        extends SingleAnnotationMessager<YamlGenerate, YamlGenerateMesseger> {
    private final Resource ftl;
    private final Resource yml;

    static YamlGenerateMesseger from(final Messager messager,
            final Element element) {
        return new YamlGenerateMesseger(messager, element, null, null, null,
                null);
    }

    private YamlGenerateMesseger(final Messager messager,
            final Element element, final AnnotationMirror mirror,
            final AnnotationValue value, final Resource ftl,
            final Resource yml) {
        super(YamlGenerate.class, messager, element, mirror, value);
        this.ftl = ftl;
        this.yml = yml;
    }

    @Override
    public YamlGenerateMesseger withAnnotation(final AnnotationMirror mirror,
            final AnnotationValue value) {
        return new YamlGenerateMesseger(messager, element, mirror, value, ftl,
                yml);
    }

    public YamlGenerateMesseger withTemplate(final Resource ftl) {
        return new YamlGenerateMesseger(messager, element, mirror, value, ftl,
                yml);
    }

    public YamlGenerateMesseger withYaml(final Resource yml) {
        return new YamlGenerateMesseger(messager, element, mirror, value, ftl,
                yml);
    }

    @Override
    protected MessageArgs messageArgs(final String format,
            final Object... args) {
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
        } else {
            xFormat = "%s(%s): " + format;
            xArgs = new Object[2 + args.length];
            xArgs[0] = yml.getDescription();
            xArgs[1] = ftl.getDescription();
            arraycopy(args, 0, xArgs, 2, args.length);
        }

        return super.messageArgs(xFormat, xArgs);
    }
}
