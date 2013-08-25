package hm.binkley.configuration;

import java.util.Properties;

/**
 * {@code PropertiesFetcher} needs documentation.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public interface PropertiesFetcher<E extends Exception> {
    Properties fetch()
            throws E;
}
