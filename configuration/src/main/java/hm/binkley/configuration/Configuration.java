package hm.binkley.configuration;

import javax.annotation.Nonnull;

/**
 * {@code Configuration} needs documentation.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public interface Configuration<T extends Configuration<T, K, V, E>, K, V, E extends Exception> {
    V lookup(@Nonnull final K key)
            throws E;
}
