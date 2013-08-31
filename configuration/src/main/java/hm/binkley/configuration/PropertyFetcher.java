package hm.binkley.configuration;

import javax.annotation.Nonnull;
import java.util.Properties;

/**
 * {@code PropertyFetcher} needs documentation.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public interface PropertyFetcher<K, V, E extends Exception> {
    V fetch(@Nonnull final Properties properties, @Nonnull final K key)
            throws E;

    @Nonnull
    String describe(@Nonnull final K key);
}
