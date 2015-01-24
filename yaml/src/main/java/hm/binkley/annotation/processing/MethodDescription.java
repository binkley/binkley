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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static hm.binkley.annotation.processing.YamlGenerateProcessor.typeOf;
import static java.lang.Character.isLowerCase;
import static java.lang.String.format;
import static java.util.regex.Pattern.compile;

/**
 * {@code MethodDescription} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public final class MethodDescription {
    private static final Pattern keySeps = compile("\\W+");

    public final String name;
    public final String type;
    public final Object value;

    /** @todo Use fail() and return null */
    public static MethodDescription methodDescription(final String key,
            final String type, final Object value) {
        if (null == value) {
            if (null == type)
                throw new IllegalStateException(
                        format("Missing value and type for '%s", key));
            switch (type) {
            case "bool":
                return new MethodDescription(camelCase(key), "bool", false);
            case "int":
                return new MethodDescription(camelCase(key), "int", 0);
            case "float":
                return new MethodDescription(camelCase(key), "float", 0.0d);
            case "seq":
                return new MethodDescription(camelCase(key), "seq",
                        new ArrayList<>(0));
            case "pairs":
                return new MethodDescription(camelCase(key), "pairs",
                        new LinkedHashMap<>(0));
            default:
                return new MethodDescription(camelCase(key), type, null);
            }
        } else if (null != type) {
            final String actualType = typeOf(value);
            switch (type) {
            case "str":
            case "bool":
            case "int":
            case "float":
            case "seq":
            case "pairs":
                if (!actualType.equals(type))
                    throw new IllegalStateException(
                            format("Conflicting type and value for '%s': '%s' vs '%s'",
                                    key, type, actualType));
            }
            // TODO: How to check UDTs?
        }

        return new MethodDescription(camelCase(key), typeOf(value), value);
    }

    private MethodDescription(final String name, final String type,
            final Object value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    private static String camelCase(final CharSequence in) {
        final String u = keySeps.matcher(in).replaceAll("_");
        return isLowerCase(u.charAt(0)) ? LOWER_UNDERSCORE.to(LOWER_CAMEL, u)
                : UPPER_UNDERSCORE.to(UPPER_CAMEL, u);
    }
}
