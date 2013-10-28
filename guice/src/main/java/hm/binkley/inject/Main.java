/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.inject;

import com.google.inject.Injector;
import hm.binkley.inject.JOptSimpleModule.OptionKey;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.aeonbits.owner.Config;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.ServiceLoader;

import static com.google.common.base.Functions.toStringFunction;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Maps.transformValues;
import static com.google.inject.Guice.createInjector;
import static hm.binkley.inject.JOptSimpleModule.jOptSimpleModule;
import static hm.binkley.inject.JOptSimpleModule.mapWith;
import static hm.binkley.inject.OwnerModule.ownerModule;
import static java.lang.String.format;

/**
 * {@code Main} is a sample guing together the Guice modules for a trivial application bootstrap.
 * <p/>
 * For a full-featured library see <a href="https://github.com/Netflix/governator/wiki">Governator</a>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public abstract class Main<C extends Config> {
    private final String prefix;

    public static void main(final String... args) {
        final Main main = getOnlyElement(ServiceLoader.load(Main.class));
        final JOptSimpleModule jOptSimpleModule = jOptSimpleModule(args);
        main.addOptions(jOptSimpleModule.parser());
        final Injector preGuice = createInjector(jOptSimpleModule);
        final OptionSet options = preGuice.getInstance(OptionSet.class);
        final OwnerModule ownerModule = ownerModule(main.configType(), mapOf(options, main.prefix));
        preGuice.createChildInjector(ownerModule, new LifecycleModule(),
                new MetaInfServicesModule()).injectMembers(main);
    }

    protected Main() {
        this(null);
    }

    protected Main(@Nullable final String prefix) {
        this.prefix = prefix;
    }

    static Map<String, String> mapOf(final OptionSet options) {
        return mapOf(options, null);
    }

    private static Map<String, String> mapOf(final OptionSet options, final String prefix) {
        return transformValues(mapWith(options, new OptionKey() {
            @Override
            public String select(final OptionSpec<?> spec) {
                final Collection<String> flags = spec.options();
                for (final String flag : flags)
                    if (1 < flag.length())
                        return null == prefix ? flag : (prefix + '.' + flag);
                throw new IllegalArgumentException(format("No usable flag: %s", flags));
            }
        }), toStringFunction());
    }

    /**
     * Declare the configuration type for the OWNER API.
     *
     * @return the config type, never missing
     */
    @Nonnull
    protected abstract Class<C> configType();

    /**
     * Configure command line parsing with JOpt-Simple.
     *
     * @param optionParser the options parser, never mising
     */
    protected abstract void addOptions(@Nonnull final OptionParser optionParser);
}
