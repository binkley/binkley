/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;
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
 */
public final class ParameterizedHelper {
    private ParameterizedHelper() {}

    /**
     * Creates a list of parmeters for the {@link Parameters} static method.
     *
     * @param ini the INI file, never mising
     * @param keys the {@link Key keys}
     *
     * @return the parameters list, never missing
     */
    @Nonnull
    public static List<Object[]> parameters(@Nonnull final Ini ini, final Key... keys) {
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
     * @see #parameters(Ini, Key...)
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
