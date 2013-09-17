package hm.binkley.util.logging;

import org.slf4j.Logger;
import org.slf4j.ext.LoggerWrapper;

/**
 * {@code MarkedLogger} needs documentation.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public class MarkedLogger
        extends LoggerWrapper {
    public MarkedLogger(final Logger logger, final String fqcn) {
        super(logger, fqcn);
    }
}
