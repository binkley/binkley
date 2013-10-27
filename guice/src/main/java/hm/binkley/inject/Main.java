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
 * {@code Main} needs documentation.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public abstract class Main {
    public static void main(final String... args) {
        final Main main = getOnlyElement(ServiceLoader.load(Main.class));
        final JOptSimpleModule jOptSimpleModule = jOptSimpleModule(args);
        main.addOptions(jOptSimpleModule.parser());
        final Injector preGuice = createInjector(jOptSimpleModule);
        final OptionSet options = preGuice.getInstance(OptionSet.class);
        final OwnerModule ownerModule = ownerModule(main.configType(), mapOf(options, null));
        preGuice.createChildInjector(ownerModule, new LifecycleModule(),
                new MetaInfServicesModule());
    }

    static Map<String, String> mapOf(final OptionSet options) {
        return mapOf(options, null);
    }

    static Map<String, String> mapOf(final OptionSet options, final String prefix) {
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

    protected abstract void addOptions(final OptionParser optionParser);

    protected abstract <C extends Config> Class<C> configType();
}
