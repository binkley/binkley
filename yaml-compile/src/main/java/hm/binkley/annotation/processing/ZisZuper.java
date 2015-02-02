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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * {@code ZisZuper} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public final class ZisZuper {
    private static final Pattern space = compile("\\s+");

    @Nonnull
    public final Names zis;
    @Nullable
    public final Names zuper;

    public static ZisZuper from(final CharSequence packaj, final String key) {
        final String name;
        final String parent;
        final String[] names = space.split(key);
        switch (names.length) {
        case 1:
            name = names[0];
            parent = null;
            break;
        case 2:
            name = names[0];
            parent = names[1];
            break;
        default:
            return null;
        }
        //noinspection ConstantConditions
        return new ZisZuper(Names.from(packaj, name, key),
                Names.from(packaj, parent, key));
    }

    @Nullable
    String parent() {
        return null == zuper || "Enum".equals(zuper.name) ? null
                : zuper.nameRelativeTo(zis);
    }

    boolean overridden(
            final Map<String, Map<String, Map<String, Object>>> methods,
            final String method) {
        return null != zuper && methods.get(zuper.fullName)
                .containsKey(method);
    }

    private ZisZuper(@Nonnull final Names zis, @Nullable final Names zuper) {
        this.zis = zis;
        this.zuper = zuper;
    }
}
