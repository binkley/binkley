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

package hm.binkley.lang;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static java.util.regex.Pattern.compile;

/**
 * {@code StringX} is additional methods for {@link String}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public final class StringX {
    private final Map<Pattern, Supplier<String>> replacements
            = new LinkedHashMap<>();

    public void addReplacement(final char symbol,
            @Nonnull final String replacement) {
        replacements.put(compile("%" + symbol), () -> replacement);
    }

    @Nonnull
    public String format(@Nonnull final String rawFormat,
            final Object... rawArgs) {
        String format = rawFormat;
        for (final Entry<Pattern, Supplier<String>> e : replacements
                .entrySet())
            format = e.getKey().matcher(format)
                    .replaceAll(e.getValue().get());

        return String.format(format, patchArrays(rawArgs));
    }

    private static Object[] patchArrays(final Object... rawArgs) {
        final List<Object> argsList = new ArrayList<>(rawArgs.length);
        asList(rawArgs).stream().
                map(a -> argsList.add(a instanceof Object[] ? Arrays
                        .toString((Object[]) a) : a));
        return argsList.toArray(new Object[rawArgs.length]);
    }
}
