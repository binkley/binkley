/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionDeclarer;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.List;

import static com.google.common.collect.Lists.asList;
import static com.google.inject.Guice.createInjector;
import static com.google.inject.name.Names.named;
import static java.util.Arrays.asList;

/**
 * {@code JOptSimpleModule} integrates JOptSimple into Guice.  Example: <pre>
 * // Bootstrap
 * final Injector bootstrap = createInjector(bootstrapJOptSimple());
 * // Bind command line String[] to option parser (internal to module)
 * final JOptSimpleModule jOptSimpleModule = bindArgs(bootstrap, args);
 * // Module exposes OptionDeclarer to configure option parsing
 * final ArgumentAcceptingOptionSpec<File> rootsOption = jOptSimpleModule.
 *         accepts("r").
 *         withRequiredArg().
 *         ofType(File.class).
 *         defaultsTo(listRoots());
 * // Optionally pre-configure the parser in another bootstrap module, to be injected if needed.
 *
 * // Final injector, only at this point is the command line parsed
 * final Injector guice = bootstrap.createChildInjector(jOptSimpleModule);
 *
 * // Use the option set
 * final OptionSet optionSet = guice.getInstance(OptionSet.class);
 * // Command line available as immutable list under "main.args"
 * out.println(guice.getInstance(
 *         Key.get(new TypeLiteral&lt;List&lt;String&gt;&gt;() {}, named("main.args"))));</pre>
 * out.println(optionSet.valuesOf(rootsOption));
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public final class JOptSimpleModule
        extends AbstractModule
        implements OptionDeclarer {
    private final OptionParser parser;
    private final String[] args;

    /** Creates a new Guice injector including a {@code JOptSimpleBootstrapModule}. */
    public static Injector bootstrapInjector(final Module... modules) {
        return createInjector(asList(new JOptSimpleBootstrapModule(), modules));
    }

    /**
     * Binds command line arguments to the given <var>parent</var> injector.
     *
     * @param parent the parent injector, never missing
     * @param args the command line args, missing if none
     *
     * @return a {@code JOptSimpleModule} usable for declaring options, never missing
     */
    @Nonnull
    public static JOptSimpleModule bindArgs(@Nonnull final Injector parent, final String... args) {
        return parent.getInstance(JOptSimpleModuleFactory.class).bind(args);
    }

    @Inject
    private JOptSimpleModule(@Nonnull final OptionParser parser, @Assisted final String... args) {
        this.parser = parser;
        this.args = args;
    }

    @Override
    protected void configure() {
        bind(new TypeLiteral<List<String>>() {}).annotatedWith(named("main.args"))
                .toInstance(asList(args));
        bind(OptionSet.class).toInstance(parser.parse(args));
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
    public OptionSpecBuilder acceptsAll(final List<String> options) {
        return parser.acceptsAll(options);
    }

    @Override
    public OptionSpecBuilder acceptsAll(final List<String> options,
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

    private interface JOptSimpleModuleFactory {
        @Nonnull
        JOptSimpleModule bind(final String... args);
    }

    private static final class JOptSimpleBootstrapModule
            extends AbstractModule {
        @Override
        protected void configure() {
            install(new FactoryModuleBuilder()
                    .implement(JOptSimpleModule.class, JOptSimpleModule.class)
                    .build(JOptSimpleModuleFactory.class));
        }
    }
}
