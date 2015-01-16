package hm.binkley.annotation.processing;

import org.springframework.core.io.Resource;

import javax.annotation.Nonnull;
import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.lang.System.arraycopy;
import static java.nio.charset.StandardCharsets.UTF_8;
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
    private static final Pattern nl = compile("\\n");

    private final Class<?> owner = YamlGenerateProcessor.class;
    private final Messager messager;
    private final Element element;
    private final AnnotationMirror mirror;
    private final AnnotationValue value;
    private final Resource ftl;
    private final Resource yml;

    static YamlGenerateMessenger from(final Messager messager,
            final Element element) {
        return new YamlGenerateMessenger(messager, element, null, null, null,
                null);
    }

    private YamlGenerateMessenger(final Messager messager,
            final Element element, final AnnotationMirror mirror,
            final AnnotationValue value, final Resource ftl,
            final Resource yml) {
        this.messager = messager;
        this.element = element;
        this.mirror = mirror;
        this.value = value;
        this.ftl = ftl;
        this.yml = yml;
    }

    YamlGenerateMessenger withAnnotation(final AnnotationMirror mirror,
            final AnnotationValue value) {
        return new YamlGenerateMessenger(messager, element, mirror, value,
                ftl, yml);
    }

    YamlGenerateMessenger withTemplate(final Resource ftl) {
        return new YamlGenerateMessenger(messager, element, mirror, value,
                ftl, yml);
    }

    YamlGenerateMessenger withYaml(final Resource yml) {
        return new YamlGenerateMessenger(messager, element, mirror, value,
                ftl, yml);
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
        if (null == cause && null == ftl && null == yml) {
            xFormat = format;
            xArgs = args;
        } else if (null == cause && null == yml) {
            xFormat = "(%s): " + format;
            xArgs = new Object[1 + args.length];
            xArgs[0] = ftl.getDescription();
            arraycopy(args, 0, xArgs, 1, args.length);
        } else if (null == cause && null == ftl) {
            throw new AssertionError("FTL cannot be null if YML present");
        } else if (null == cause) {
            xFormat = "%s(%s): " + format;
            xArgs = new Object[2 + args.length];
            xArgs[0] = yml.getDescription();
            xArgs[1] = ftl.getDescription();
            arraycopy(args, 0, xArgs, 2, args.length);
        } else if (null == ftl && null == yml) {
            xFormat = format + ": %s";
            xArgs = new Object[1 + args.length];
            arraycopy(args, 0, xArgs, 0, args.length);
            xArgs[args.length] = cause;
        } else if (null == yml) {
            xFormat = "(%s): " + format + ": %s";
            xArgs = new Object[2 + args.length];
            xArgs[0] = ftl.getDescription();
            arraycopy(args, 0, xArgs, 1, args.length);
            xArgs[1 + args.length] = cause;
        } else if (null == ftl) {
            throw new AssertionError("FTL cannot be null if YML present");
        } else {
            xFormat = "%s(%s): " + format + ": %s";
            xArgs = new Object[3 + args.length];
            xArgs[0] = yml.getDescription();
            xArgs[1] = ftl.getDescription();
            arraycopy(args, 0, xArgs, 2, args.length);
            xArgs[2 + args.length] = cause;
        }

        messager.printMessage(kind,
                format(aname.matcher(xFormat).replaceAll(owner.getName()),
                        xArgs), element, mirror, value);
        // TODO: Can I get Javac messenger to handle stacktrace?
        if (null != cause)
            cause.printStackTrace(new PrintWriter(new MessengerWriter()));
    }

    private class MessengerWriter
            extends Writer {
        private final Buffer buf = new Buffer();

        @Override
        public void write(@Nonnull final char[] cbuf, final int off,
                final int len) {
            buf.write(cbuf, off, len);
        }

        @Override
        public void flush() {
            for (final String line : nl.split(buf.toString()))
                error(line);
        }

        @Override
        public void close() {
            buf.close();
        }
    }

    private static final class Buffer
            extends ByteArrayOutputStream {
        @Override
        public void close() {
            try {
                super.close();
                buf = null;
            } catch (final IOException e) {
                throw new Error("ByteArrayOutputStream.close() threw", e);
            }
        }

        @Override
        public synchronized String toString() {
            try {
                return toString(UTF_8.name());
            } catch (final UnsupportedEncodingException e) {
                throw new Error("UTF-8 missing from JDK", e);
            }
        }

        public void write(@Nonnull final char[] cbuf, final int off,
                final int len) {
            write(cbuf, off, len, UTF_8);
        }

        public void write(@Nonnull final char[] cbuf, final int off,
                final int len, final Charset charset) {
            try {
                write(charset.encode(CharBuffer.wrap(cbuf, off, len))
                        .array());
            } catch (final IOException e) {
                throw new Error("ByteArrayOutputStream.write() threw", e);
            }
        }
    }
}
