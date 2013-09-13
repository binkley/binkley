package hm.binkley.configuration;

import com.google.common.base.Function;

import javax.annotation.Nonnull;

import static hm.binkley.configuration.Conversions.strings;
import static hm.binkley.configuration.Conversions.unchecked;
import static hm.binkley.configuration.MergedPropertiesLoader.merge;
import static hm.binkley.configuration.SpringPropertiesLoader.springPropertiesLoader;

/**
 * {@code SpringFormatConfiguration} needs documentation.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public abstract class SpringFormatConfiguration<K, V, E extends Exception>
        extends AbstractPropertyConfiguration<SpringFormatConfiguration<K, V, E>, K, V, E> {
    protected SpringFormatConfiguration(@Nonnull final Function<String, V> returns,
            @Nonnull final Function<Exception, E> exceptions, @Nonnull final String locationPattern,
            @Nonnull final String format, final Object... params) {
        super(merge(SystemPropertiesLoader.<E>systemPropertiesLoader(),
                EnvironmentLoader.<E>environmentLoader(),
                springPropertiesLoader(exceptions, locationPattern)), FormatPropertyFetcher
                .<K, V, E>formatPropertyFetcher(returns, exceptions, format, params));
    }

    public static class DefaultSpringFormatConfiguration
            extends SpringFormatConfiguration<Object, String, RuntimeException> {
        protected DefaultSpringFormatConfiguration(@Nonnull final String locationPattern,
                @Nonnull final String format, final Object... params) {
            super(strings(), unchecked(), locationPattern, format, params);
        }

        @Nonnull
        @Override
        public String lookup(@Nonnull final Object key) {
            final String value = super.lookup(key);
            if (null == value)
                throw new MissingConfigurationKeyException(misskingKeyMessage(key));
            return value;
        }
    }
}
