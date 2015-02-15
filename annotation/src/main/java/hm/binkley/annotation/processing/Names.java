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
import java.util.Objects;

/**
 * {@code Names} finds the final full name, package name and simple class name
 * for a root package and candidate class name, including a relative package
 * portion.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public final class Names {
    public final String fullName;
    public final String packaj;
    public final String name;

    /**
     * Creates a new {@code Names} from the given <var>packaj</var> and
     * relative <var>name</var>.
     *
     * @param packaj the package, never missing
     * @param name the relative name, possibly missing
     *
     * @return the new {@code Names} or {@code null} if <var>name</var> is
     * {@code null}
     */
    @Nullable
    public static Names from(@Nonnull final CharSequence packaj,
            @Nullable final String name) {
        if (null == name)
            return null;

        final String fullName;
        final String packageName;
        final String className;

        final int len = packaj.length();
        final int x = name.lastIndexOf('.');
        if (-1 == x) {
            fullName = 0 == len ? name : packaj + "." + name;
            packageName = packaj.toString();
            className = name;
        } else {
            if (0 == len) {
                fullName = name;
                packageName = name.substring(0, x);
            } else {
                fullName = packaj + "." + name;
                packageName = packaj + "." + name.substring(0, x);
            }
            className = name.substring(x + 1);
        }

        return new Names(fullName, packageName, className);
    }

    private Names(@Nonnull final String fullName,
            @Nonnull final String packaj, @Nonnull final String name) {
        this.fullName = fullName;
        this.packaj = packaj;
        this.name = name;
    }

    /**
     * Finds the simplest superclass name relative to a subclass.
     *
     * @param zis the subclass, never missing
     *
     * @return the superclas name, never missing
     */
    @Nonnull
    public String nameRelativeTo(@Nonnull final Names zis) {
        return packaj.equals(zis.packaj) ? name : fullName;
    }

    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        final Names that = (Names) o;
        return Objects.equals(fullName, that.fullName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullName);
    }
}
