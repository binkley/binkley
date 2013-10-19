/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.inject;

import com.google.inject.AbstractModule;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import javax.annotation.Nonnull;

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
}
