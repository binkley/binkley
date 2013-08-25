package hm.binkley.configuration;

import javax.annotation.Nonnull;
import java.util.Properties;

/**
 * {@code PropertyGetter} needs documentation.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public interface PropertyGetter<E extends Exception> {
    String get(@Nonnull final Properties properties, @Nonnull final Object key)
            throws E;
}
