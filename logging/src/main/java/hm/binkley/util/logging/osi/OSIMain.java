/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util.logging.osi;

import static hm.binkley.util.logging.LoggerUtil.refreshLogback;
import static java.lang.System.clearProperty;
import static java.lang.System.setProperty;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * {@code OSIMain} demonstrates OSI logging.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public final class OSIMain {
    public static void main(final String... args) {
        // This is the only configuration needed:
        OSI.enable();

        getLogger("example").error("Hi, mom!");

        // Turn logging of logging on
        setProperty("logback.debug", "true");
        refreshLogback();
        getLogger("example").error("Hi, mom!");

        // Turn it back off
        clearProperty("logback.debug");
        refreshLogback();
        getLogger("example").error("Hi, mom!");
    }
}
