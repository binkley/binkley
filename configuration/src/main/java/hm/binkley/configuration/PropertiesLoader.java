package hm.binkley.configuration;

import java.util.Properties;

/**
 * {@code PropertiesLoader} needs documentation.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public interface PropertiesLoader<E extends Exception> {
    Properties load()
            throws E;
}
