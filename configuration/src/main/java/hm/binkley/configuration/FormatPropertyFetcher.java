package hm.binkley.configuration;

import com.google.common.base.Function;

import javax.annotation.Nonnull;
import java.util.Properties;

import static java.lang.String.format;
import static java.lang.System.arraycopy;

/**
 * {@code FormatPropertyFetcher} needs documentation.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public class FormatPropertyFetcher<K, V, E extends Exception>
        implements PropertyFetcher<K, V, E> {
    private final Function<String, V> returns;
    private final Function<Exception, E> exceptions;
    private final String format;
    private final Object[] params;

    public FormatPropertyFetcher(@Nonnull final Function<String, V> returns,
            @Nonnull final Function<Exception, E> exceptions, @Nonnull final String format,
            final Object... params) {
        this.returns = returns;
        this.exceptions = exceptions;
        this.format = format;
        this.params = params;
    }

    @Override
    public V fetch(@Nonnull final Properties properties, @Nonnull final K key)
            throws E {
        try {
            final Object[] params = new Object[this.params.length + 1];
            arraycopy(this.params, 0, params, 0, this.params.length);
            params[this.params.length] = key;
            return returns.apply(properties.getProperty(format(format, params)));
        } catch (final Exception e) {
            throw exceptions.apply(e);
        }
    }
}
