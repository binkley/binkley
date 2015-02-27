/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org/>.
 */

package hm.binkley.annotation.processing;

import hm.binkley.util.StringX;

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
import java.lang.annotation.Annotation;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

import static com.google.common.base.Throwables.getRootCause;
import static hm.binkley.util.Arrays.cat;
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
public abstract class SingleAnnotationMessager<A extends Annotation, M extends SingleAnnotationMessager<A, M>> {
    private static final Pattern nl = compile("\\n");

    protected final StringX stringx = new StringX();
    protected final Class<A> annoType;
    protected final Messager messager;
    protected final Element element;
    protected final AnnotationMirror mirror;
    protected final AnnotationValue value;

    protected SingleAnnotationMessager(final Class<A> annoType,
            final Messager messager, final Element element,
            final AnnotationMirror mirror, final AnnotationValue value) {
        this.annoType = annoType;
        this.messager = messager;
        this.element = element;
        this.mirror = mirror;
        this.value = value;

        stringx.put('@',
                null == mirror ? annoType.getName() : mirror.toString());
    }

    public abstract M withAnnotation(final AnnotationMirror mirror,
            final AnnotationValue value);

    public void error(final Exception cause, final String format,
            final Object... args) {
        message(cause, ERROR, format, args);
    }

    public void error(final String format, final Object... args) {
        message(null, ERROR, format, args);
    }

    public void warning(final String format, final Object... args) {
        message(null, WARNING, format, args);
    }

    public void note(final String format, final Object... args) {
        message(null, NOTE, format, args);
    }

    private void message(final Exception cause, final Kind kind,
            final String format, final Object... args) {
        messager.printMessage(kind, message(cause, format, args), element,
                mirror);
        // TODO: Can I get Javac messenger to handle stacktrace?
        if (null != cause) {
            cause.printStackTrace();
            cause.printStackTrace(new PrintWriter(new MessengerWriter()));
        }
    }

    protected static final class MessageArgs {
        private final String format;
        private final Object[] args;

        private MessageArgs(final String format, final Object... args) {
            this.format = format;
            this.args = args;
        }
    }

    public String annoFormat(final String rawFormat,
            final Object... rawArgs) {
        return stringx.format(rawFormat, rawArgs);
    }

    protected MessageArgs messageArgs(final Exception cause,
            final String format, final Object... args) {
        return new MessageArgs(format, args);
    }

    private String message(final Exception cause, final String format,
            final Object... args) {
        // Give subclass a chance to modify these before we do
        final MessageArgs margs = messageArgs(cause, format, args);
        final String xFormat;
        final Object[] xArgs;
        if (null == cause) {
            xFormat = margs.format;
            xArgs = margs.args;
        } else {
            xFormat = margs.format + ": %s at %s";
            // Include nearest location, e.g., NPE which has no details
            xArgs = cat(margs.args, cause,
                    getRootCause(cause).getStackTrace()[0]);
        }

        return annoFormat(xFormat, xArgs);
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
