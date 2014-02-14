/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.net.URL;

import static ch.qos.logback.core.joran.util.ConfigurationWatchListUtil.getMainWatchURL;

/**
 * {@code LoggerUtil} has utility methods for SLF4J loggers.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public final class LoggerUtil {
    private LoggerUtil() {
    }

    /**
     * Programatically changes the log <var>level</var> if <var>logger</var> is a logback logger.
     *
     * @param logger the logback logger, never missing
     * @param level the new level, never missing
     *
     * @throws ClassCastException if <var>logger</var> is not a logback logger
     */
    public static void setLevel(@Nonnull final Logger logger, @Nonnull final Level level) {
        ((ch.qos.logback.classic.Logger) logger).setLevel(level);
    }

    /** Resets the global logack, forcing reread of configuration. */
    public static void refreshLogback() {
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();
        final URL url = getMainWatchURL(context);
        if (null == url)
            throw new IllegalStateException("Missing logback configuration");
        final JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        try {
            configurator.doConfigure(url);
            configurator.registerSafeConfiguration();
        } catch (final JoranException e) {
            throw new IllegalStateException("Bad logback configuration: " + url, e);
        }
    }
}
