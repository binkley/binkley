/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util.logging.osi;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.WarnStatus;
import ch.qos.logback.core.util.Loader;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

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

    private static void stopLoggingThreads0(final LoggerContext context) {
        context.stop();
    }

    /**
     * @todo Do not use {@link ContextInitializer#autoConfig()} directly - assumes sysprops
     */
    private static void reconfigureLogging0(final LoggerContext context)
    throws JoranException {
        context.reset();
        context.getStatusManager().clear();
        new ContextInitializer(context).autoConfig();
    }

    public void reconfigure()
            throws JoranException {
        reconfigureLogging0(context);
    }

    @Override
    public void close() {
        stopLoggingThreads0(context);
    }

    /**
     * @param logbackConfigFile
     *
     * @return
     *
     * @todo Use {@link ContextInitializer#findConfigFileURLFromSystemProperties(ClassLoader,
     * boolean)} when 3-arg
     * @todo Throw rather than return {@code null}, do not swallow exceptions
     */
    @Nonnull
    private URL x(@Nonnull final String logbackConfigFile) {
        URL result = null;
        try {
            result = new URL(logbackConfigFile);
            return result;
        } catch (final MalformedURLException e) {
            // so, resource is not a URL:
            // attempt to get the resource from the class path
            result = Loader.getResource(logbackConfigFile, getClass().getClassLoader());
            if (result != null) {
                return result;
            }
            final File f = new File(logbackConfigFile);
            if (f.exists() && f.isFile()) {
                try {
                    result = f.toURI().toURL();
                    return result;
                } catch (MalformedURLException e1) {
                }
            }
        } finally {
            // statusOnResourceSearch(logbackConfigFile, classLoader, result);
            if (result == null) {
                context.getStatusManager()
                        .add(new InfoStatus("Could NOT find resource [" + logbackConfigFile + "]",
                                context));
            } else {
                context.getStatusManager().add(new InfoStatus(
                        "Found resource [" + logbackConfigFile + "] at [" + result.toString() + "]",
                        context));
                // multiplicityWarning(logbackConfigFile, getClass().getClassLoader());
                Set<URL> urlSet = null;
                try {
                    urlSet = Loader.getResourceOccurrenceCount(logbackConfigFile,
                            getClass().getClassLoader());
                } catch (final IOException e) {
                    context.getStatusManager().add(new ErrorStatus(
                            "Failed to get url list for resource [" + logbackConfigFile + "]",
                            context, e));
                }
                if (urlSet != null && urlSet.size() > 1) {
                    context.getStatusManager().add(new WarnStatus("Resource [" + logbackConfigFile
                            + "] occurs multiple times on the classpath.", context));
                    for (final URL url : urlSet) {
                        context.getStatusManager().add(new WarnStatus(
                                "Resource [" + logbackConfigFile + "] occurs at [" + url.toString()
                                        + "]", context));
                    }
                }
            }
        }
        return null;
    }
}
