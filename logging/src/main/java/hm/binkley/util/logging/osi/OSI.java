/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util.logging.osi;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import hm.binkley.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import static hm.binkley.util.logging.osi.OSI.SystemProperty.LOGBACK_CONFIGURATION_FILE;
import static hm.binkley.util.logging.osi.OSI.SystemProperty.LOG_LEVEL;
import static java.lang.String.format;
import static java.lang.System.clearProperty;
import static java.lang.System.getProperty;
import static java.lang.System.out;
import static java.lang.System.setProperty;
import static java.util.Arrays.asList;

/**
 * {@code OSI} enable OSI logging for simple cases.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public final class OSI {
    /**
     * Enable OSI logging using the default configuration file, "osi-logback.xml" as found on the
     * class path.  Control configuration through use of other {@link SystemProperty OSI system
     * properties}. <p> Must be called before first use of logback.
     * <p>
     * Do not show status of the logging system.
     */
    public static void enable() {
        enable(false);
    }

    /**
     * Enable OSI logging using the default configuration file, "osi-logback.xml" as found on the
     * class path.  Control configuration through use of other {@link SystemProperty OSI system
     * properties}. <p> Must be called before first use of logback.
     *
     * @param show if {@code true} log the status of the logging system including setup details.
     */
    public static void enable(final boolean show) {
        SLF4JBridgeHandler.install();
        LOGBACK_CONFIGURATION_FILE.set("osi-logback.xml", false);
        if (!show)
            return;
        asList(SystemProperty.values()).forEach(out::println);
        StatusPrinter.print((LoggerContext) LoggerFactory.getILoggerFactory());
    }

    /**
     * {@code SystemProperty} defines system properties used by OSI. Use {@link #set(String,
     * boolean)} and {@link #unset()} to control these properties.
     */
    public enum SystemProperty {
        /**
         * Sets the logback configuration file, rarely changed except for testing.  Default is
         * "osi-logback.xml".
         *
         * @see #enable()
         */
        LOGBACK_CONFIGURATION_FILE("logback.configurationFile"),
        /**
         * As an alternative to setting system properties, put properties here.  Default is
         * "osi-logback.properties" in the classpath root.  These cannot, however, override these
         * system properties which are used before the properties resource is loaded:
         * <ul><li>logback.configurationFile</li> <li>logback.propertiesResource</li>
         * <li>logback.debug</li></ul>
         */
        LOGBACK_PROPERTIES_RESOURCE("logback.propertiesResources"),
        /**
         * Sets a custom style file for logging, rarely changed.  Default is
         * "osi-logback-style.properties".
         */
        LOGBACK_STYLES_FILE("logback.stylesFile"),
        /**
         * Sets the default logging style.  See "osi-logback-styles.properties" for details. Default
         * is "standard".
         */
        LOGBACK_STYLE("logback.style"),
        /**
         * Sets the file of additional included logging directives.  This is often changed (or one
         * named "osi-logback-included.xml" is provided in the application class path) to control
         * logging such as changing log levels.  Default is "osi-logback-included.xml".
         */
        LOGBACK_INCLUDED_FILE("logback.includedFile"),
        /** Enables logback debugging.  Default is "false". */
        LOGBACK_DEBUG("logback.debug"),
        /**
         * Adjusts the general logging level when no more specific level is configured for a logger.
         * Default is "WARN".
         *
         * @see Level
         */
        LOG_LEVEL("log.level"),
        /** Sets the root appender.  Default is "console". */
        LOGBACK_ROOT_APPENDER("logback.rootAppender");
        private static final Map<SystemProperty, String> totem = new EnumMap<>(
                SystemProperty.class);
        @Nonnull
        private final String key;

        SystemProperty(@Nonnull final String key) {
            this.key = key;
        }

        /**
         * Gets the key string used with system properties.
         *
         * @return the key string, never missing
         */
        @Nonnull
        public final String key() {
            return key;
        }

        /**
         * Gets the value of the corresponding system property.
         *
         * @return the system property value or {@code null} if undefined
         */
        @Nullable
        public final String get() {
            return getProperty(key);
        }

        /**
         * Sets the value of the corresponding system property, or clears it if {@code null}.  May
         * be called only once without an interving call to {@link #unset()} for the same key. <p>
         * If <var>override</var> it {@code true} can replace an existing system property value to
         * be restored by {@link #unset()}.  Will always return {@code true} in this case. <p> If
         * <var>override</var> is {@code false} will only set a system property that does not
         * already exist, and returns {@code true}.  But if the system property already has a value,
         * does nothing and returns {@code false}. <p> The <var>override</var> variant exists to aid
         * testing which might replace a value, or production which should respect explictly set
         * system properties from the command line.
         *
         * @param value the system property value or {@code null} to clear
         * @param override if {@code true} (temporarily) replaces existing system property value
         *
         * @return {@code true} if the system properties were modified
         *
         * @throws IllegalStateException if already set
         */
        public final boolean set(@Nullable final String value, final boolean override) {
            final String existing = getProperty(key);
            if (totem.containsKey(this))
                throw new IllegalStateException(
                        format("%s: Already modified, unset first: %s", this, existing));
            if (null != value && null != existing && !override)
                return false;
            if (null == value)
                clearProperty(key);
            else
                setProperty(key, value);
            totem.put(this, existing);
            return true;
        }

        /**
         * Unsets the value to the corresponding system property, restoring the previous value or
         * clearing if not present.  May be called only after a call to {@link #set(String,
         * boolean)} for the same key.
         *
         * @throws IllegalStateException if not set
         */
        public final void unset() {
            if (!totem.containsKey(this))
                throw new IllegalStateException(format("%s: Not set", this));
            final String value = totem.get(this);
            if (null == value)
                clearProperty(key);
            else
                setProperty(key, value);
            totem.remove(this);
        }

        @Nonnull
        @Override
        public final String toString() {
            return format("%s(%s)=%s", name(), key, Objects.toString(getProperty(key), "<default>"));
        }
    }

    public static void main(final String... args) {
        LOG_LEVEL.set("ERROR", false);
        enable(true);
        final Logger log = LoggerFactory.getLogger(OSI.class);
        log.warn("Won't log");
        log.error("Will log");
        log.error("Example stacktrace: {}", "Hi, mom!", new IOException("Nothing actually is wrong"));
    }
}
