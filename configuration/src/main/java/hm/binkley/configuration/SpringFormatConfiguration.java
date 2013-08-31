package hm.binkley.configuration;

import com.google.common.base.Function;

import javax.annotation.Nonnull;

import static hm.binkley.configuration.Conversions.strings;
import static hm.binkley.configuration.Conversions.unchecked;
import static hm.binkley.configuration.MergedPropertiesLoader.merge;
import static java.lang.String.format;

/**
 * {@code SpringFormatConfiguration} needs documentation.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public class SpringFormatConfiguration<K, V, E extends Exception>
        extends AbstractPropertyConfiguration<SpringFormatConfiguration<K, V, E>, K, V, E> {
    public SpringFormatConfiguration(@Nonnull final Function<String, V> returns,
            @Nonnull final Function<Exception, E> exceptions, @Nonnull final String locationPattern,
            @Nonnull final String format, final Object... params) {
        super(merge(SystemPropertiesLoader.<E>systemPropertiesLoader(),
                EnvironmentLoader.<E>environmentLoader(),
                new SpringPropertiesLoader<>(exceptions, locationPattern)),
                new FormatPropertyFetcher<K, V, E>(returns, exceptions, format, params));
    }

    public static DefaultSpringFormatConfiguration springFormatConfiguration(
            @Nonnull final String locationPattern, @Nonnull final String format,
            final Object... params) {
        return new DefaultSpringFormatConfiguration(locationPattern, format, params);
    }

    public static class DefaultSpringFormatConfiguration
            extends SpringFormatConfiguration<Object, String, RuntimeException> {
        public DefaultSpringFormatConfiguration(@Nonnull final String locationPattern,
                @Nonnull final String format, final Object... params) {
            super(strings(), unchecked(), locationPattern, format, params);
        }

        @Nonnull
        @Override
        public String lookup(@Nonnull final Object key) {
            final String value = super.lookup(key);
            if (null == value)
                throw new RuntimeException(
                        format("Cannot find %s in: %s", fetcher.describe(key), loader.describe()));
            return value;
        }
    }
}
