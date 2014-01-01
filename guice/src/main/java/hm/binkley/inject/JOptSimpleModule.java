/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.inject;

import com.google.inject.AbstractModule;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionDeclarer;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * {@code JOptSimpleModule} is light-weight wiring of JOptSimple command-line parsing.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public final class JOptSimpleModule
        extends AbstractModule {
    private final OptionParser parser;
    private final String[] args;

    /**
     * Creates a new {@code JOptSimpleModule} {@link Builder}.
     *
     * @return the new builder, never missing
     */
    @Nonnull
    public static Builder builder() {
        return new Builder();
    }

    private JOptSimpleModule(final OptionParser parser, final String[] args) {
        this.parser = parser;
        this.args = args;
    }

    @Override
    protected void configure() {
        bind(OptionSet.class).toInstance(parser.parse(args));
    }

    /** {@code Builder} sets up command-line parsing for {@link JOptSimpleModule}. */
    public static final class Builder
            implements OptionDeclarer {
        private final OptionParser parser = new OptionParser();

        private Builder() {
        }

        public JOptSimpleModule parse(final String... args) {
            return new JOptSimpleModule(parser, args);
        }

        @Override
        public OptionSpecBuilder accepts(final String option) {
            return parser.accepts(option);
        }

        @Override
        public OptionSpecBuilder accepts(final String option, final String description) {
            return parser.accepts(option, description);
        }

        @Override
        public OptionSpecBuilder acceptsAll(final Collection<String> options) {
            return parser.acceptsAll(options);
        }

        @Override
        public OptionSpecBuilder acceptsAll(final Collection<String> options,
                final String description) {
            return parser.acceptsAll(options, description);
        }

        @Override
        public NonOptionArgumentSpec<String> nonOptions() {
            return parser.nonOptions();
        }

        @Override
        public NonOptionArgumentSpec<String> nonOptions(final String description) {
            return parser.nonOptions(description);
        }

        @Override
        public void posixlyCorrect(final boolean setting) {
            parser.posixlyCorrect(setting);
        }

        @Override
        public void allowsUnrecognizedOptions() {
            parser.allowsUnrecognizedOptions();
        }

        @Override
        public void recognizeAlternativeLongOptions(final boolean recognize) {
            parser.recognizeAlternativeLongOptions(recognize);
        }
    }
}
