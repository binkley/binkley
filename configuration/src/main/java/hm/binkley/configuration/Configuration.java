package hm.binkley.configuration;

import javax.annotation.Nonnull;

/**
 * {@code Configuration} needs documentation.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public interface Configuration<T extends Configuration<T, E>, E extends Exception> {
    String lookup(@Nonnull final Object key)
            throws E;
}
