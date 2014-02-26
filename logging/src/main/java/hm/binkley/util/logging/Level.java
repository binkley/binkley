/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util.logging;

import org.slf4j.Logger;
import org.slf4j.Marker;

/**
 * {@code Level} inverts calls to SLF4J providing an object for methods corresponding to each
 * logging level.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public enum Level {
    /** All log calls are ignored. */
    NONE {
        @Override
        public boolean isEnabled(final Logger logger) {
            return false;
        }

        @Override
        public void log(final Logger logger, final String msg) {
        }

        @Override
        public void log(final Logger logger, final String format, final Object arg) {
        }

        @Override
        public void log(final Logger logger, final String format, final Object arg1,
                final Object arg2) {
        }

        @Override
        public void log(final Logger logger, final String format, final Object... arguments) {
        }

        @Override
        public void log(final Logger logger, final String msg, final Throwable t) {
        }

        @Override
        public boolean isEnabled(final Logger logger, final Marker marker) {
            return false;
        }

        @Override
        public void log(final Logger logger, final Marker marker, final String msg) {
        }

        @Override
        public void log(final Logger logger, final Marker marker, final String format,
                final Object arg) {
        }

        @Override
        public void log(final Logger logger, final Marker marker, final String format,
                final Object arg1, final Object arg2) {
        }

        @Override
        public void log(final Logger logger, final Marker marker, final String format,
                final Object... argArray) {
        }

        @Override
        public void log(final Logger logger, final Marker marker, final String msg,
                final Throwable t) {
        }
    },
    /** All log calls are at the TRACE level. */
    TRACE {
        @Override
        public boolean isEnabled(final Logger logger) {
            return logger.isTraceEnabled();
        }

        @Override
        public void log(final Logger logger, final String msg) {
            logger.trace(msg);
        }

        @Override
        public void log(final Logger logger, final String format, final Object arg) {
            logger.trace(format, arg);
        }

        @Override
        public void log(final Logger logger, final String format, final Object arg1,
                final Object arg2) {
            logger.trace(format, arg1, arg2);
        }

        @Override
        public void log(final Logger logger, final String format, final Object... arguments) {
            logger.trace(format, arguments);
        }

        @Override
        public void log(final Logger logger, final String msg, final Throwable t) {
            logger.trace(msg, t);
        }

        @Override
        public boolean isEnabled(final Logger logger, final Marker marker) {
            return logger.isTraceEnabled(marker);
        }

        @Override
        public void log(final Logger logger, final Marker marker, final String msg) {
            logger.trace(marker, msg);
        }

        @Override
        public void log(final Logger logger, final Marker marker, final String format,
                final Object arg) {
            logger.trace(marker, format, arg);
        }

        @Override
        public void log(final Logger logger, final Marker marker, final String format,
                final Object arg1, final Object arg2) {
            logger.trace(marker, format, arg1, arg2);
        }

        @Override
        public void log(final Logger logger, final Marker marker, final String format,
                final Object... argArray) {
            logger.trace(marker, format, argArray);
        }

        @Override
        public void log(final Logger logger, final Marker marker, final String msg,
                final Throwable t) {
            logger.trace(marker, msg, t);
        }
    },
    /** All log calls are at the DEBUG level. */
    DEBUG {
        @Override
        public boolean isEnabled(final Logger logger) {
            return logger.isDebugEnabled();
        }

        @Override
        public void log(final Logger logger, final String msg) {
            logger.debug(msg);
        }

        @Override
        public void log(final Logger logger, final String format, final Object arg) {
            logger.debug(format, arg);
        }

        @Override
        public void log(final Logger logger, final String format, final Object arg1,
                final Object arg2) {
            logger.debug(format, arg1, arg2);
        }

        @Override
        public void log(final Logger logger, final String format, final Object... arguments) {
            logger.debug(format, arguments);
        }

        @Override
        public void log(final Logger logger, final String msg, final Throwable t) {
            logger.debug(msg, t);
        }

        @Override
        public boolean isEnabled(final Logger logger, final Marker marker) {
            return logger.isDebugEnabled(marker);
        }

        @Override
        public void log(final Logger logger, final Marker marker, final String msg) {
            logger.debug(marker, msg);
        }

        @Override
        public void log(final Logger logger, final Marker marker, final String format,
                final Object arg) {
            logger.debug(marker, format, arg);
        }

        @Override
        public void log(final Logger logger, final Marker marker, final String format,
                final Object arg1, final Object arg2) {
            logger.debug(marker, format, arg1, arg2);
        }

        @Override
        public void log(final Logger logger, final Marker marker, final String format,
                final Object... argArray) {
            logger.debug(marker, format, argArray);
        }

        @Override
        public void log(final Logger logger, final Marker marker, final String msg,
                final Throwable t) {
            logger.debug(marker, msg, t);
        }
    },
    /** All log calls are at the INFO level. */
    INFO {
        @Override
        public boolean isEnabled(final Logger logger) {
            return logger.isInfoEnabled();
        }

        @Override
        public void log(final Logger logger, final String msg) {
            logger.info(msg);
        }

        @Override
        public void log(final Logger logger, final String format, final Object arg) {
            logger.info(format, arg);
        }

        @Override
        public void log(final Logger logger, final String format, final Object arg1,
                final Object arg2) {
            logger.info(format, arg1, arg2);
        }

        @Override
        public void log(final Logger logger, final String format, final Object... arguments) {
            logger.info(format, arguments);
        }

        @Override
        public void log(final Logger logger, final String msg, final Throwable t) {
            logger.info(msg, t);
        }

        @Override
        public boolean isEnabled(final Logger logger, final Marker marker) {
            return logger.isInfoEnabled(marker);
        }

        @Override
        public void log(final Logger logger, final Marker marker, final String msg) {
            logger.info(marker, msg);
        }

        @Override
        public void log(final Logger logger, final Marker marker, final String format,
                final Object arg) {
            logger.info(marker, format, arg);
        }

        @Override
        public void log(final Logger logger, final Marker marker, final String format,
                final Object arg1, final Object arg2) {
            logger.info(marker, format, arg1, arg2);
        }

        @Override
        public void log(final Logger logger, final Marker marker, final String format,
                final Object... argArray) {
            logger.info(marker, format, argArray);
        }

        @Override
        public void log(final Logger logger, final Marker marker, final String msg,
                final Throwable t) {
            logger.info(marker, msg, t);
        }
    },
    /** All log calls are at the WARN level. */
    WARN {
        @Override
        public boolean isEnabled(final Logger logger) {
            return logger.isWarnEnabled();
        }

        @Override
        public void log(final Logger logger, final String msg) {
            logger.warn(msg);
        }

        @Override
        public void log(final Logger logger, final String format, final Object arg) {
            logger.warn(format, arg);
        }

        @Override
        public void log(final Logger logger, final String format, final Object arg1,
                final Object arg2) {
            logger.warn(format, arg1, arg2);
        }

        @Override
        public void log(final Logger logger, final String format, final Object... arguments) {
            logger.warn(format, arguments);
        }

        @Override
        public void log(final Logger logger, final String msg, final Throwable t) {
            logger.warn(msg, t);
        }

        @Override
        public boolean isEnabled(final Logger logger, final Marker marker) {
            return logger.isWarnEnabled(marker);
        }

        @Override
        public void log(final Logger logger, final Marker marker, final String msg) {
            logger.warn(marker, msg);
        }

        @Override
        public void log(final Logger logger, final Marker marker, final String format,
                final Object arg) {
            logger.warn(marker, format, arg);
        }

        @Override
        public void log(final Logger logger, final Marker marker, final String format,
                final Object arg1, final Object arg2) {
            logger.warn(marker, format, arg1, arg2);
        }

        @Override
        public void log(final Logger logger, final Marker marker, final String format,
                final Object... argArray) {
            logger.warn(marker, format, argArray);
        }

        @Override
        public void log(final Logger logger, final Marker marker, final String msg,
                final Throwable t) {
            logger.warn(marker, msg, t);
        }
    },
    /** All log calls are at the ERROR level. */
    ERROR {
        @Override
        public boolean isEnabled(final Logger logger) {
            return logger.isErrorEnabled();
        }

        @Override
        public void log(final Logger logger, final String msg) {
            logger.error(msg);
        }

        @Override
        public void log(final Logger logger, final String format, final Object arg) {
            logger.error(format, arg);
        }

        @Override
        public void log(final Logger logger, final String format, final Object arg1,
                final Object arg2) {
            logger.error(format, arg1, arg2);
        }

        @Override
        public void log(final Logger logger, final String format, final Object... arguments) {
            logger.error(format, arguments);
        }

        @Override
        public void log(final Logger logger, final String msg, final Throwable t) {
            logger.error(msg, t);
        }

        @Override
        public boolean isEnabled(final Logger logger, final Marker marker) {
            return logger.isErrorEnabled(marker);
        }

        @Override
        public void log(final Logger logger, final Marker marker, final String msg) {
            logger.error(marker, msg);
        }

        @Override
        public void log(final Logger logger, final Marker marker, final String format,
                final Object arg) {
            logger.error(marker, format, arg);
        }

        @Override
        public void log(final Logger logger, final Marker marker, final String format,
                final Object arg1, final Object arg2) {
            logger.error(marker, format, arg1, arg2);
        }

        @Override
        public void log(final Logger logger, final Marker marker, final String format,
                final Object... argArray) {
            logger.error(marker, format, argArray);
        }

        @Override
        public void log(final Logger logger, final Marker marker, final String msg,
                final Throwable t) {
            logger.error(marker, msg, t);
        }
    };

    /**
     * Is the logger instance enabled for this level?
     *
     * @return True if this Logger is enabled for this level, false otherwise.
     */
    public abstract boolean isEnabled(final Logger logger);

    /**
     * Log a message at this level.
     *
     * @param msg the message string to be logged
     */
    public abstract void log(final Logger logger, String msg);

    /**
     * Log a message at this level according to the specified format and argument. <p/> <p>This form
     * avoids superfluous object creation when the logger is disabled for this level. </p>
     *
     * @param format the format string
     * @param arg the argument
     */
    public abstract void log(final Logger logger, String format, Object arg);

    /**
     * Log a message at this level according to the specified format and arguments. <p/> <p>This
     * form avoids superfluous object creation when the logger is disabled for this level. </p>
     *
     * @param format the format string
     * @param arg1 the first argument
     * @param arg2 the second argument
     */
    public abstract void log(final Logger logger, String format, Object arg1, Object arg2);

    /**
     * Log a message at this level according to the specified format and arguments. <p/> <p>This
     * form avoids superfluous string concatenation when the logger is disabled for this level.
     * However, this variant incurs the hidden (and relatively small) cost of creating an {@code
     * Object[]} before invoking the method, even if this logger is disabled for this level. The
     * variants taking {@link #log(Logger, String, Object) one} and {@link #log(Logger, String,
     * Object, Object) two} arguments exist solely in order to avoid this hidden cost.</p>
     *
     * @param format the format string
     * @param arguments a list of 3 or more arguments
     */
    public abstract void log(final Logger logger, String format, Object... arguments);

    /**
     * Log an exception (throwable) at this level with an accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param t the exception (throwable) to log
     */
    public abstract void log(final Logger logger, String msg, Throwable t);

    /**
     * Similar to {@link #isEnabled(Logger)} method except that the marker data is also taken into
     * account.
     *
     * @param marker The marker data to take into consideration
     *
     * @return True if this Logger is enabled for this level, false otherwise.
     */
    public abstract boolean isEnabled(final Logger logger, Marker marker);

    /**
     * Log a message with the specific Marker at this level.
     *
     * @param marker the marker data specific to this log statement
     * @param msg the message string to be logged
     */
    public abstract void log(final Logger logger, Marker marker, String msg);

    /**
     * This method is similar to {@link #log(Logger, String, Object)} method except that the marker
     * data is also taken into consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg the argument
     */
    public abstract void log(final Logger logger, Marker marker, String format, Object arg);

    /**
     * This method is similar to {@link #log(Logger, String, Object, Object)} method except that the
     * marker data is also taken into consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg1 the first argument
     * @param arg2 the second argument
     */
    public abstract void log(final Logger logger, Marker marker, String format, Object arg1,
            Object arg2);

    /**
     * This method is similar to {@link #log(Logger, String, Object...)} method except that the
     * marker data is also taken into consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param argArray an array of arguments
     */
    public abstract void log(final Logger logger, Marker marker, String format, Object... argArray);

    /**
     * This method is similar to {@link #log(Logger, String, Throwable)} method except that the
     * marker data is also taken into consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param msg the message accompanying the exception
     * @param t the exception (throwable) to log
     */
    public abstract void log(final Logger logger, Marker marker, String msg, Throwable t);
}
