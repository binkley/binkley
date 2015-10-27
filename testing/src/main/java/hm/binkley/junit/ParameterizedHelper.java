/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.junit;

import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;
import org.ini4j.Wini;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.util.function.Function.identity;

/**
 * {@code ParameterizedHelper} assists in writing JUnit {@link Parameterized} tests.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Rename to ParameterizedTestHelper
 */
public final class ParameterizedHelper {
    private ParameterizedHelper() {}

    /**
     * Creates a list of parmeters for the {@link Parameters} static method.
     * <p>
     * For each {@code Object[]} the first element is the {@link Section#getName() section name}.
     * The remaining elements are supplied by each {@link Key key} in order formed by {@link
     * Section#fetch(Object) fetching} the section value for the key and applying the {@link Key#get
     * value function}.  Missing keys in the INI produce a {@code null} value.
     * <p>
     * For example use {@code Parameterized} like this: <pre>
     * &#64;Parameters(name = "{index}: {0}")
     * public static Collection&lt;Object[]&gt; parameters() {
     *     return ParameterizedHelper.parameters(new Ini(...),
     *             Key.of("a"), Key.of("b", BigDecimal::new));
     * }
     * </pre> JUnit will then include the INI section name in describing failed cases.
     * <p>
     * Please read the documentation on {@link Config INI configuration} to control processing of
     * the INI source.  For example, the default {@link Ini} class respects escape sequences (e.g.,
     * "\t" for TAB); the {@link Wini} class does not.
     * <p>
     * The name "parametersFrom" was choosen to aid static import.
     *
     * @param ini the INI file, never mising
     * @param keys the {@link Key keys}
     *
     * @return the parameters list, never missing
     */
    @Nonnull
    public static List<Object[]> parametersFrom(@Nonnull final Ini ini, final Key... keys) {
        final List<Object[]> parameters = new ArrayList<>();
        for (final Section section : ini.values()) {
            final Object[] array = new Object[1 + keys.length];
            array[0] = section.getName();
            for (int i = 0; i < keys.length; i++) {
                final Key key = keys[i];
                final String value = section.fetch(key.name);
                array[1 + i] = null == value ? null : key.get.apply(value);
            }
            parameters.add(array);
        }
        return parameters;
    }

    /**
     * Tuple for defining INI section keys and how to map their values to test parameters.
     *
     * @see #parametersFrom(Ini, Key...)
     */
    public static final class Key {
        public final String name;
        public final Function<String, ?> get;

        /**
         * Creates a new {@code Key} for the given section key <var>name</var> and value
         * <var>get</var> function.
         *
         * @param name the section key name, never missing
         *
         * @return the new key, never missing
         *
         * @see #of(String)
         */
        @Nonnull
        public static Key of(@Nonnull final String name, @Nonnull final Function<String, ?> get) {
            return new Key(name, get);
        }

        /**
         * Creates a new {@code Key} for the given section key <var>name</var> and an {@link
         * Function#identity() identity} value function.
         *
         * @param name the section key name, never missing
         *
         * @return the new key, never missing
         *
         * @see #of(String, Function)
         */
        @Nonnull
        public static Key of(@Nonnull final String name) {
            return of(name, identity());
        }

        private Key(final String name, final Function<String, ?> get) {
            this.name = name;
            this.get = get;
        }
    }
}
