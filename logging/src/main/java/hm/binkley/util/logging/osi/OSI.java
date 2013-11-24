/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util.logging.osi;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;

import static java.lang.System.setProperty;
import static org.slf4j.LoggerFactory.getILoggerFactory;

/**
 * {@code OSI} needs documentation.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 * @todo Support singleton and non-singleton modes
 * @todo Implement {@link java.lang.AutoCloseable} when non-singleton
 */
public final class OSI
        implements AutoCloseable {
    private final LoggerContext context = new LoggerContext();

    /**
     * @todo Support non-singleton version - bits of logback are package private for this
     */
    public static void enableLogging() {
        setProperty("logback.configurationFile", "osi-logback.xml");
    }

    public static void reconfigureLogging()
            throws JoranException {
        reconfigureLogging0((LoggerContext) getILoggerFactory());
    }

    public static void stopLoggingThreads() {
        stopLoggingThreads0((LoggerContext) getILoggerFactory());
    }

    private static void stopLoggingThreads0(final LoggerContext loggerContext) {
        loggerContext.stop();
    }

    /**
     * @todo Do not use {@link ContextInitializer#autoConfig()} directly - assumes sysprops
     */
    private static void reconfigureLogging0(final LoggerContext loggerContext)
            throws JoranException {
        loggerContext.reset();
        loggerContext.getStatusManager().clear();
        new ContextInitializer(loggerContext).autoConfig();
    }

    public void reconfigure()
            throws JoranException {
        reconfigureLogging0(context);
    }

    @Override
    public void close() {
        stopLoggingThreads0(context);
    }
}
