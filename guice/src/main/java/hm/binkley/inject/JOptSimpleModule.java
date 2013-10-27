/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.inject;

import com.google.inject.AbstractModule;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static joptsimple.internal.Objects.ensureNotNull;

/**
 * {@code JOptSimpleModule} is light-weight wiring of JOptSimple command-line parsing.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public final class JOptSimpleModule
        extends AbstractModule {
    private final OptionParser parser = new OptionParser();
    private final String[] args;

    /**
     * Creates a new Guice module for option parsing.
     *
     * @param args the command line args, usually the parameters of {@code main()}
     *
     * @return the Guice module, never missing
     */
    public static JOptSimpleModule jOptSimpleModule(final String... args) {
        return new JOptSimpleModule(args);
    }

    private JOptSimpleModule(final String[] args) {
        this.args = args;
    }

    /**
     * Create an new map of keys to option values.
     *
     * @param selector the function for finding keys for option specs, never missing
     *
     * @return the map, never missing
     *
     * @deprecated Part of pull request for jopt-simple
     */
    @Deprecated
    public static Map<String, Object> mapWith(final OptionSet options, final OptionKey selector) {
        ensureNotNull(selector);

        // Alternate implementation might present a view rather than a copy
        final Map<String, Object> map = new LinkedHashMap<>();
        for (final OptionSpec<?> spec : options.specs())
            map.put(selector.select(spec), valueFor(options, spec));
        return unmodifiableMap(map);
    }

    private static Object valueFor(final OptionSet options, final OptionSpec<?> spec) {
        final List<?> values = spec.values(options);
        switch (values.size()) {
            case 0:
                return true;
            case 1:
                return values.get(0);
            default:
                return values;
        }
    }

    /**
     * The option parser for access to {@link OptionParser#accepts(String) accepts} and friends.  Do
     * not call {@link OptionParser#parse(String...) parse} or its ilk; this is called in {@link
     * #configure(com.google.inject.Binder) configure} during binding.
     *
     * @return the option parser, never missing
     *
     * @deprecated To be replaced when {@code OptionParser} provides a facade interface
     */
    @Nonnull
    public OptionParser parser() {
        return parser;
    }

    @Override
    protected void configure() {
        bind(OptionSet.class).toInstance(parser.parse(args));
    }

    /**
     * Pick a key to represent an option for {@link #mapWith}.
     *
     * @deprecated Part of pull request for jopt-simple
     */
    @Deprecated
    public interface OptionKey {
        /**
         * Select a string key for the given <var>spec</var> in {@link #mapWith}.
         * <p/>
         * A typical implementation would use the long form of aliases.  If "-d" and "--debug" are
         * aliased flags, the key would be "debug".  This is also a good place to modify keys, say
         * "my.app.debug" for the "debug" option when integrating options into other features such
         * as properties.
         *
         * @param spec the option spec, never missing
         *
         * @return the string key
         */
        String select(OptionSpec<?> spec);
    }
}
