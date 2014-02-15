/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util.logging.osi;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static hm.binkley.util.logging.osi.OSI.SystemProperty.LOGBACK_CONFIGURATION_FILE;
import static java.lang.String.format;
import static java.lang.System.clearProperty;
import static java.lang.System.getProperty;
import static java.lang.System.setProperty;

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
     */
    public static void enable() {
        LOGBACK_CONFIGURATION_FILE.set("osi-logback.xml", false);
    }

    /**
     * {@code SystemProperty} defines system properties used by OSI. Use {@link #set(String,
     * boolean)} and {@link #unset()} to control these properties.
     */
    public static enum SystemProperty {
        /**
         * Sets the logback configuration file, rarely changed except for testing.
         *
         * @see #enable()
         */
        LOGBACK_CONFIGURATION_FILE("logback.configurationFile"),
        /** Sets a custom style file for logging, rarely changed. */
        LOGBACK_STYLES_FILE("logback.stylesFile"),
        /**
         * Sets the default logging style.  The three default styles are: <ul> <li>standard</li>
         * <li>short-deals</li> <li>long-deals</li> </ul> See "osi-logback-styles.properties" for
         * details.
         */
        LOGBACK_STYLE("logback.style"),
        /**
         * Sets the file of additional included logging directives.  This is often changed (or one
         * named "osi-logback-included.xml" is provided in the application class path) to control
         * logging such as changing log levels.
         */
        LOGBACK_INCLUDED_FILE("logback.includedFile"),
        /** Enables logback debugging. */
        LOGBACK_DEBUG("logback.debug");
        private static final Map<SystemProperty, String> totem = new HashMap<>(values().length);
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
            final String previous;
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
            return String.format("%s(%s)", name(), key);
        }
    }
}
