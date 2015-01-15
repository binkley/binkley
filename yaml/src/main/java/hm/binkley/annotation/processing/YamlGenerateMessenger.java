package hm.binkley.annotation.processing;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.lang.System.arraycopy;
import static java.util.regex.Pattern.compile;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;
import static javax.tools.Diagnostic.Kind.WARNING;

/**
 * {@code GenerateMessenger} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
final class YamlGenerateMessenger {
    private static final Pattern aname = compile("%@");

    private final Class<?> owner;
    private final Messager messager;
    private final Element element;
    private final AnnotationMirror mirror;
    private final AnnotationValue value;
    private final String ftl;
    private final String yml;

    YamlGenerateMessenger(final Class<?> owner, final Messager messager,
            final Element element, final AnnotationMirror mirror) {
        this(owner, messager, element, mirror, null, null, null);
    }

    private YamlGenerateMessenger(final Class<?> owner,
            final Messager messager, final Element element,
            final AnnotationMirror mirror, final AnnotationValue value,
            final String ftl, final String yml) {
        this.owner = owner;
        this.messager = messager;
        this.element = element;
        this.mirror = mirror;
        this.value = value;
        this.ftl = ftl;
        this.yml = yml;
    }

    YamlGenerateMessenger with(final String ftl, final String yml) {
        return new YamlGenerateMessenger(owner, messager, element, mirror,
                value, ftl, yml);
    }

    YamlGenerateMessenger with(final String yml) {
        return new YamlGenerateMessenger(owner, messager, element, mirror,
                value, ftl, yml);
    }

    void error(final Exception cause, final String format,
            final Object... args) {
        message(cause, ERROR, format, args);
    }

    void error(final String format, final Object... args) {
        message(null, ERROR, format, args);
    }

    void warning(final String format, final Object... args) {
        message(null, WARNING, format, args);
    }

    void note(final String format, final Object... args) {
        message(null, NOTE, format, args);
    }

    private void message(final Exception cause, final Kind kind,
            final String format, final Object... args) {
        final String xFormat;
        final Object[] xArgs;
        if (null == ftl && null == cause) {
            xFormat = format;
            xArgs = args;
        } else if (null == cause) {
            xFormat = "%s(%s): " + format;
            xArgs = new Object[2 + args.length];
            xArgs[0] = yml;
            xArgs[1] = ftl;
            arraycopy(args, 0, xArgs, 2, args.length);
        } else if (null == ftl) {
            xFormat = format + ": %s";
            xArgs = new Object[1 + args.length];
            arraycopy(args, 0, xArgs, 0, args.length);
            xArgs[args.length] = cause;
        } else {
            xFormat = "%s(%s): " + format + ": %s";
            xArgs = new Object[3 + args.length];
            xArgs[0] = yml;
            xArgs[1] = ftl;
            arraycopy(args, 0, xArgs, 2, args.length);
            xArgs[2 + args.length] = cause;
        }

        messager.printMessage(kind,
                format(aname.matcher(xFormat).replaceAll(owner.getName()),
                        xArgs), element, mirror, value);
        // TODO: Can I get Javac messenger to handle stacktrace?
        if (null != cause)
            cause.printStackTrace();
    }
}
