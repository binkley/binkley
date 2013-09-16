package hm.binkley.configuration;

import com.google.common.base.Function;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Properties;

import static java.lang.String.format;
import static java.lang.System.arraycopy;

/**
 * {@code FormatPropertyFetcher} needs documentation.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public final class FormatPropertyFetcher<K, V, E extends Exception>
        implements PropertyFetcher<K, V, E> {
    private final Function<String, V> returns;
    private final Function<Exception, E> exceptions;
    private final String format;
    private final Object[] params;

    private FormatPropertyFetcher(@Nonnull final Function<String, V> returns,
            @Nonnull final Function<Exception, E> exceptions, @Nonnull final String format,
            final Object... params) {
        this.returns = returns;
        this.exceptions = exceptions;
        this.format = format;
        this.params = params;
    }

    public static <K, V, E extends Exception> FormatPropertyFetcher<K, V, E> formatPropertyFetcher(
            @Nonnull final Function<String, V> returns,
            @Nonnull final Function<Exception, E> exceptions, @Nonnull final String format,
            final Object... params) {
        return new FormatPropertyFetcher<>(returns, exceptions, format, params);
    }

    @Override
    public V fetch(@Nonnull final Properties properties, @Nonnull final K key)
            throws E {
        try {
            return returns.apply(properties.getProperty(of(key)));
        } catch (final Exception e) {
            throw exceptions.apply(e);
        }
    }

    @Nonnull
    @Override
    public String describe(@Nonnull final K key) {
        return of(key);
    }

    @Nonnull
    @Override
    public String toString() {
        return format("%s(%s%s)", getClass().getSimpleName(), format, Arrays.toString(params));
    }

    private String of(final K key) {
        final Object[] params = new Object[this.params.length + 1];
        arraycopy(this.params, 0, params, 0, this.params.length);
        params[this.params.length] = key;
        return format(format, params);
    }
}
