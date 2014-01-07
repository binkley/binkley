/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util.logging.osi;

import org.slf4j.LoggerFactory;

/**
 * {@code OSIMain} demonstrates OSI logging.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public final class OSIMain {
    public static void main(final String... args) {
        // This is the only configuration needed:
        System.setProperty("logback.configurationFile", "osi-logback.xml");

        LoggerFactory.getLogger("example").info("Hi, mom!");
    }
}
