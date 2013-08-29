package hm.binkley.configuration;

import com.google.common.base.Function;

import javax.annotation.Nonnull;

/**
 * {@code SpringFormatConfiguration} needs documentation.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public class SpringFormatConfiguration<T extends SpringFormatConfiguration<T, K, V, E>, K, V, E extends Exception>
        extends AbstractPropertyConfiguration<SpringFormatConfiguration<T, K, V, E>, K, V, E> {
    public SpringFormatConfiguration(@Nonnull final Function<String, V> returns,
            @Nonnull final Function<Exception, E> exceptions, @Nonnull final String locationPattern,
            @Nonnull final String format, final Object... params) {
        super(new SpringPropertiesLoader<>(exceptions, locationPattern),
                new FormatPropertyFetcher<K, V, E>(returns, exceptions, format, params));
    }
}
