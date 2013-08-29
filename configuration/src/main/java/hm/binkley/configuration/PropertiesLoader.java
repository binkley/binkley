package hm.binkley.configuration;

import javax.annotation.Nonnull;
import java.util.Properties;

/**
 * {@code PropertiesLoader} needs documentation.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public interface PropertiesLoader<E extends Exception> {
    @Nonnull
    Properties load()
            throws E;
}
