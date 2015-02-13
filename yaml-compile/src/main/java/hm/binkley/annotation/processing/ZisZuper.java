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
import javax.lang.model.element.Element;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static javax.lang.model.element.Modifier.FINAL;

/**
 * {@code ZisZuper} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public final class ZisZuper {
    private static final Pattern space = compile("\\s+");
    private static final Map<String, ZisZuper> classes
            = new LinkedHashMap<>();

    @Nonnull
    public final String key;
    @Nonnull
    public final Names zis;
    @Nullable
    public final Names zuper;
    @Nonnull
    private final Element root;

    /**
     * Constructs a new {@link ZisZuper} for the given parameters.
     *
     * @param packaj the package for <var>key</var>, never missing
     * @param key the space-separated list of class and optional parent, never
     * missing
     * @param root the optional root element, for non-YAML parents
     *
     * @return the new {@code ZisZuper}, or {@code null} if <var>key</var> is
     * invalid
     */
    @Nullable
    public static ZisZuper from(@Nonnull final CharSequence packaj,
            @Nonnull final String key, @Nullable final Element root) {
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
        final ZisZuper zz = new ZisZuper(key, Names.from(packaj, name, key),
                Names.from(packaj, parent, key), root);
        classes.put(zz.zis.fullName, zz);
        return zz;
    }

    boolean override(final ZisZuper names, final String method,
            final Map<String, List<String>> methods) {
        methods.computeIfAbsent(names.zis.fullName, k -> new ArrayList<>())
                .add(method);
        for (ZisZuper parent = classes.get(names.parent()); null != parent;
                parent = classes.get(parent.parent()))
            if (methods.get(parent.zis.fullName).contains(method))
                return true;
        return false;
    }

    @Nullable
    String parent() {
        if (null == zuper)
            return root.getModifiers().contains(FINAL) ? null
                    : root.toString();
        if ("Enum".equals(zuper.name))
            return null;
        return zuper.nameRelativeTo(zis);
    }

    @Nonnull
    String kind() {
        return null == zuper ? root.getKind().name().toLowerCase() : "class";
    }

    /** @todo Undo hack for non-YAML base class */
    boolean overridden(
            final Map<String, Map<String, Map<String, Object>>> methods,
            final String method) {
        // Work out root overrides
        return null != zuper && methods.get(zuper.fullName)
                .containsKey(method);
    }

    private ZisZuper(@Nonnull final String key, @Nonnull final Names zis,
            @Nullable final Names zuper, @Nonnull Element root) {
        this.key = key;
        this.zis = zis;
        this.zuper = zuper;
        this.root = root;
    }
}
