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

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static java.lang.String.format;

/**
 * {@code Loaded} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public abstract class Loaded<T> {
    private static final Pattern BACKSLASH = Pattern.compile("\\\\");
    public final String where;
    public final Resource whence;
    public final T what;

    protected Loaded(final String where, final Resource whence,
            final T what) {
        this.where = where;
        this.whence = whence;
        this.what = what;
    }

    protected static String path(final String pattern, final Resource whence,
            final List<String> roots)
            throws IOException {
        if (whence instanceof ClassPathResource)
            return format("classpath:/%s(%s)",
                    ((ClassPathResource) whence).getPath(), pattern);
        else if (whence instanceof FileSystemResource)
            return format("%s(%s)", shorten(whence.getURI(), roots), pattern);
        else
            return format("%s(%s)", whence.getURI().toString(), pattern);
    }

    public abstract String where();

    public final FormatArgs describe() {
        final FormatArgs fa = new FormatArgs();
        final String whence = BACKSLASH.matcher(this.whence.getDescription()).
                replaceAll("/");
        fa.add("%s", whence);
        // Ignore leading SLASH when checking if path already in description
        if (!whence.contains(where.substring(1)))
            fa.add("(%s)", where);

        return fa;
    }

    @Override
    public final String toString() {
        final FormatArgs fa = describe();
        if (null != what)
            fa.add(": %s", what);

        return fa.toString();
    }

    private static String shorten(final URI uri, final List<String> roots) {
        final String path = uri.getPath();
        return roots.stream().
                filter(path::endsWith).
                map(root -> "classpath:/" + root).
                findFirst().
                orElse(uri.toString());
    }

    private static final class FormatArgs {
        private final StringBuilder format = new StringBuilder();
        private final List<Object> args = new ArrayList<>();

        public void add(final String format, final Object arg) {
            this.format.append(format);
            args.add(arg);
        }

        @Override
        public String toString() {
            return format(format.toString(), args.toArray());
        }
    }
}
