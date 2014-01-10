/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util.logging.osi;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

import java.net.URL;

import static ch.qos.logback.core.joran.util.ConfigurationWatchListUtil.getMainWatchURL;
import static ch.qos.logback.core.util.StatusPrinter.printInCaseOfErrorsOrWarnings;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * {@code OSI} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public final class OSI {
    public static void reload()
            throws JoranException {
        reload(getMainWatchURL(getRootLogger().getLoggerContext()));
    }

    public static void reload(final URL url)
            throws JoranException {
        final ch.qos.logback.classic.Logger root = getRootLogger();
        final LoggerContext context = root.getLoggerContext();
        final JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        context.reset();
        configurator.doConfigure(url);
        printInCaseOfErrorsOrWarnings(context);
    }

    private static ch.qos.logback.classic.Logger getRootLogger() {
        return (ch.qos.logback.classic.Logger) getLogger(ROOT_LOGGER_NAME);
    }

    private OSI() {}
}
