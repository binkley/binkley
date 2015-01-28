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

package hm.binkley.util;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * {@code StringX} is additional methods for {@link String}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 * @todo Access specifier args, expose to replacement
 * @todo Support %[argument_index$][flags][width][.precision]conversion
 */
public final class StringX {
    // %[argument_index$][flags][width][.precision][t]conversion
    private static final Pattern customFormatSpecifier = compile(
            "%(\\d+\\$)?([-#+ 0,(<]*)?(\\d+)?(\\.\\d+)?([tT])?(.)");
    private static final Pattern percent = compile("%");

    private final Map<Character, Function<SpecifierArgs, FormatResult>>
            formattings;

    public StringX() {
        formattings = new HashMap<>();
    }

    public StringX(final int size) {
        formattings = new HashMap<>(size);
    }

    public static final class FormatResult {
        private final String formatted;
        private final int consumed;

        public FormatResult(final String formatted, final int consumed,
                final boolean reformatting) {
            if (reformatting)
                throw new UnsupportedOperationException("UNIMPLEMENTED");
            this.formatted = reformatting ? formatted
                    : percent.matcher(formatted).replaceAll("%%");
            this.consumed = consumed;
        }
    }

    public static final class SpecifierArgs {
        public final Integer index;
        public final String flags;
        public final Integer width;
        public final Integer precision;
        public final String t;
        public final Character conversion;
        public final Object[] args;
        public final int n;

        private SpecifierArgs(final Matcher matcher, final Object[] args,
                final int n) {
            index = index(matcher);
            flags = matcher.group(2);
            width = width(matcher);
            precision = precision(matcher);
            t = matcher.group(5);
            final String conversion = matcher.group(6);
            this.conversion = null == conversion ? null
                    : conversion.charAt(0);
            this.args = args;
            this.n = n;
        }

        @SuppressWarnings("unchecked")
        public <T> T arg() {
            return (T) (null == index ? args[n] : args[index - 1]);
        }

        private int forward() {
            return '%' == conversion ? 0 : 1 - lookBack();
        }

        private int forward(final FormatResult result) {
            return result.consumed - lookBack();
        }

        private int lookBack() {
            return flags.contains("<") ? 1 : 0;
        }

        private static Integer index(final Matcher matcher) {
            final String index = matcher.group(1);
            return null == index ? null
                    : Integer.valueOf(index.substring(0, index.length() - 1));
        }

        private static Integer width(final Matcher matcher) {
            final String width = matcher.group(3);
            return null == width ? null : Integer.valueOf(width);
        }

        private static Integer precision(final Matcher matcher) {
            final String precision = matcher.group(4);
            return null == precision ? null
                    : Integer.valueOf(precision.substring(1));
        }
    }

    public void put(final char conversion,
            @Nonnull final String replacement) {
        put(conversion, sargs -> new FormatResult(replacement, 0, false));
    }

    public void put(final char conversion,
            @Nonnull final Function<SpecifierArgs, FormatResult> convert) {
        if (null != formattings.putIfAbsent(conversion, convert))
            throw badConversion(conversion);
    }

    /**
     * @see Formatter
     * @see Matcher#replaceAll(String)
     */
    @Nonnull
    public String format(@Nonnull final String rawFormat,
            final Object... rawArgs) {
        final Matcher matcher = customFormatSpecifier.matcher(rawFormat);
        if (!matcher.find())
            return rawFormat;
        int n = 0;
        final StringBuffer format = new StringBuffer();
        final boolean[] holes = new boolean[rawArgs.length];
        do {
            final SpecifierArgs sargs = new SpecifierArgs(matcher, rawArgs,
                    n);
            final Function<SpecifierArgs, FormatResult> formatting
                    = formattings.get(sargs.conversion);
            if (null == formatting)
                n += sargs.forward();
            else {
                final FormatResult result = formatting.apply(sargs);
                matcher.appendReplacement(format, result.formatted);
                final int forward = sargs.forward(result);
                for (int i = 0; i < forward; ++i)
                    holes[n + i] = true;
                n += forward;
            }
        } while (matcher.find());
        matcher.appendTail(format);

        return String.format(format.toString(),
                patchArrays(punch(rawArgs, holes)));
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    private static Object[] punch(final Object[] rawArgs,
            final boolean[] holes) {
        final Object[] args = new Object[rawArgs.length];
        int n = 0;
        for (int i = 0; i < rawArgs.length; i++) {
            if (holes[i])
                continue;
            args[n] = rawArgs[i];
            ++n;
        }
        return args;
    }

    private static Object[] patchArrays(final Object... args) {
        for (int i = 0; i < args.length; i++) {
            final Object arg = args[i];
            if (arg instanceof Object[])
                args[i] = Arrays.toString((Object[]) arg);
        }
        return args;
    }

    private static IllegalArgumentException badConversion(
            final char conversion) {
        // Use IllegalFormatException but it has package-private ctor
        return new IllegalArgumentException(
                String.format("Conversion '%c' is already defined",
                        conversion));
    }
}
