package hm.binkley.configuration;

import com.google.common.base.Function;

import javax.annotation.Nonnull;

import static hm.binkley.configuration.Conversions.strings;
import static hm.binkley.configuration.Conversions.unchecked;
import static hm.binkley.configuration.MergedPropertiesLoader.merge;

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

    public static SpringFormatConfiguration<Object, String, RuntimeException> springFormatConfiguration(
            @Nonnull final String locationPattern, @Nonnull final String format,
            final Object... params) {
        return new SpringFormatConfiguration<>(strings(), unchecked(), locationPattern, format,
                params);
    }
}
