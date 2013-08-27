package hm.binkley.configuration;

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
public final class FormatPropertyFetcher
        implements PropertyFetcher<RuntimeException> {
    private final String format;
    private final Object[] params;

    public FormatPropertyFetcher(@Nonnull final String format, final Object... params) {
        this.format = format;
        this.params = params;
    }

    @Override
    public String fetch(@Nonnull final Properties properties, @Nonnull final Object key) {
        final Object[] params = new Object[this.params.length + 1];
        arraycopy(this.params, 0, params, 0, this.params.length);
        params[this.params.length] = key;
        return format(format, params);
    }
}
