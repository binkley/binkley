package lab.osi;

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
public final class OSI {
    public static void enableLogging() {
        setProperty("logback.configurationFile", "osi-logback.xml");
    }

    public static void reconfigureLogging()
            throws JoranException {
        final LoggerContext loggerContext = (LoggerContext) getILoggerFactory();
        loggerContext.reset();
        loggerContext.getStatusManager().clear();
        new ContextInitializer(loggerContext).autoConfig();
    }

    public static void stopLoggingThreads() {
        ((LoggerContext) getILoggerFactory()).stop();
    }
}
