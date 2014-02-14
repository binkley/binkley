/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util.logging;

import ch.qos.logback.classic.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.ext.LoggerWrapper;

import javax.annotation.Nonnull;

import static ch.qos.logback.classic.Level.DEBUG;
import static ch.qos.logback.classic.Level.ERROR;
import static ch.qos.logback.classic.Level.INFO;
import static ch.qos.logback.classic.Level.TRACE;
import static ch.qos.logback.classic.Level.WARN;
import static java.lang.String.format;
import static java.lang.System.out;

/**
 * {@code MinimalLogger} complains about trivial logging.  When a logger supports only a given level
 * or greater, attempts to log at a lesser level throw {@code IllegalStateException}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public class MinimumLogger
        extends LoggerWrapper {
    private final Level minimum;

    /**
     * Constructs a new {@code MinimalLogger} for the given parameters.
     *
     * @param logger the logger to delegate to, never missing
     * @param minimum the minimum, non-trivial level of logging, never missing
     */
    public MinimumLogger(@Nonnull final Logger logger, @Nonnull final Level minimum) {
        this(logger, MinimumLogger.class.getName(), minimum);
    }

    /**
     * Constructs a new {@code MinimalLogger} for the given parameters suitable as a base class.
     *
     * @param logger the logger to delegate to, never missing
     * @param fqcn the fully-qualified class name of the extending logger, never missing
     * @param minimum the minimum, non-trivial level of logging, never missing
     */
    public MinimumLogger(@Nonnull final Logger logger, @Nonnull final String fqcn,
            @Nonnull final Level minimum) {
        super(logger, fqcn);
        this.minimum = minimum;
    }

    @Override
    public boolean isTraceEnabled() {
        return TRACE.isGreaterOrEqual(minimum) && logger.isTraceEnabled();
    }

    @Override
    public boolean isTraceEnabled(final Marker marker) {
        return TRACE.isGreaterOrEqual(minimum) && logger.isTraceEnabled(marker);
    }

    @Override
    public void trace(final String msg) {
        require(TRACE);
        super.trace(msg);
    }

    @Override
    public void trace(final String format, final Object arg) {
        require(TRACE);
        super.trace(format, arg);
    }

    @Override
    public void trace(final String format, final Object arg1, final Object arg2) {
        require(TRACE);
        super.trace(format, arg1, arg2);
    }

    @Override
    public void trace(final String format, final Object... args) {
        require(TRACE);
        super.trace(format, args);
    }

    @Override
    public void trace(final String msg, final Throwable t) {
        require(TRACE);
        super.trace(msg, t);
    }

    @Override
    public void trace(final Marker marker, final String msg) {
        require(TRACE);
        super.trace(marker, msg);
    }

    @Override
    public void trace(final Marker marker, final String format, final Object arg) {
        require(TRACE);
        super.trace(marker, format, arg);
    }

    @Override
    public void trace(final Marker marker, final String format, final Object arg1,
            final Object arg2) {
        require(TRACE);
        super.trace(marker, format, arg1, arg2);
    }

    @Override
    public void trace(final Marker marker, final String format, final Object... args) {
        require(TRACE);
        super.trace(marker, format, args);
    }

    @Override
    public void trace(final Marker marker, final String msg, final Throwable t) {
        require(TRACE);
        super.trace(marker, msg, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return DEBUG.isGreaterOrEqual(minimum) && logger.isDebugEnabled();
    }

    @Override
    public boolean isDebugEnabled(final Marker marker) {
        return DEBUG.isGreaterOrEqual(minimum) && logger.isDebugEnabled(marker);
    }

    @Override
    public void debug(final String msg) {
        require(DEBUG);
        super.debug(msg);
    }

    @Override
    public void debug(final String format, final Object arg) {
        require(DEBUG);
        super.debug(format, arg);
    }

    @Override
    public void debug(final String format, final Object arg1, final Object arg2) {
        require(DEBUG);
        super.debug(format, arg1, arg2);
    }

    @Override
    public void debug(final String format, final Object... argArray) {
        require(DEBUG);
        super.debug(format, argArray);
    }

    @Override
    public void debug(final String msg, final Throwable t) {
        require(DEBUG);
        super.debug(msg, t);
    }

    @Override
    public void debug(final Marker marker, final String msg) {
        require(DEBUG);
        super.debug(marker, msg);
    }

    @Override
    public void debug(final Marker marker, final String format, final Object arg) {
        require(DEBUG);
        super.debug(marker, format, arg);
    }

    @Override
    public void debug(final Marker marker, final String format, final Object arg1,
            final Object arg2) {
        require(DEBUG);
        super.debug(marker, format, arg1, arg2);
    }

    @Override
    public void debug(final Marker marker, final String format, final Object... argArray) {
        require(DEBUG);
        super.debug(marker, format, argArray);
    }

    @Override
    public void debug(final Marker marker, final String msg, final Throwable t) {
        require(DEBUG);
        super.debug(marker, msg, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return INFO.isGreaterOrEqual(minimum) && logger.isInfoEnabled();
    }

    @Override
    public boolean isInfoEnabled(final Marker marker) {
        return INFO.isGreaterOrEqual(minimum) && logger.isInfoEnabled(marker);
    }

    @Override
    public void info(final String msg) {
        require(INFO);
        super.info(msg);
    }

    @Override
    public void info(final String format, final Object arg) {
        require(INFO);
        super.info(format, arg);
    }

    @Override
    public void info(final String format, final Object arg1, final Object arg2) {
        require(INFO);
        super.info(format, arg1, arg2);
    }

    @Override
    public void info(final String format, final Object... args) {
        require(INFO);
        super.info(format, args);
    }

    @Override
    public void info(final String msg, final Throwable t) {
        require(INFO);
        super.info(msg, t);
    }

    @Override
    public void info(final Marker marker, final String msg) {
        require(INFO);
        super.info(marker, msg);
    }

    @Override
    public void info(final Marker marker, final String format, final Object arg) {
        require(INFO);
        super.info(marker, format, arg);
    }

    @Override
    public void info(final Marker marker, final String format, final Object arg1,
            final Object arg2) {
        require(INFO);
        super.info(marker, format, arg1, arg2);
    }

    @Override
    public void info(final Marker marker, final String format, final Object... args) {
        require(INFO);
        super.info(marker, format, args);
    }

    @Override
    public void info(final Marker marker, final String msg, final Throwable t) {
        require(INFO);
        super.info(marker, msg, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return WARN.isGreaterOrEqual(minimum) && logger.isWarnEnabled();
    }

    @Override
    public boolean isWarnEnabled(final Marker marker) {
        return WARN.isGreaterOrEqual(minimum) && logger.isWarnEnabled(marker);
    }

    @Override
    public void warn(final String msg) {
        require(WARN);
        super.warn(msg);
    }

    @Override
    public void warn(final String format, final Object arg) {
        require(WARN);
        super.warn(format, arg);
    }

    @Override
    public void warn(final String format, final Object arg1, final Object arg2) {
        require(WARN);
        super.warn(format, arg1, arg2);
    }

    @Override
    public void warn(final String format, final Object... args) {
        require(WARN);
        super.warn(format, args);
    }

    @Override
    public void warn(final String msg, final Throwable t) {
        require(WARN);
        super.warn(msg, t);
    }

    @Override
    public void warn(final Marker marker, final String msg) {
        require(WARN);
        super.warn(marker, msg);
    }

    @Override
    public void warn(final Marker marker, final String format, final Object arg) {
        require(WARN);
        super.warn(marker, format, arg);
    }

    @Override
    public void warn(final Marker marker, final String format, final Object arg1,
            final Object arg2) {
        require(WARN);
        super.warn(marker, format, arg1, arg2);
    }

    @Override
    public void warn(final Marker marker, final String format, final Object... args) {
        require(WARN);
        super.warn(marker, format, args);
    }

    @Override
    public void warn(final Marker marker, final String msg, final Throwable t) {
        require(WARN);
        super.warn(marker, msg, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return WARN.isGreaterOrEqual(minimum) && logger.isWarnEnabled();
    }

    @Override
    public boolean isErrorEnabled(final Marker marker) {
        return WARN.isGreaterOrEqual(minimum) && logger.isWarnEnabled(marker);
    }

    @Override
    public void error(final String msg) {
        require(ERROR);
        super.error(msg);
    }

    @Override
    public void error(final String format, final Object arg) {
        require(ERROR);
        super.error(format, arg);
    }

    @Override
    public void error(final String format, final Object arg1, final Object arg2) {
        require(ERROR);
        super.error(format, arg1, arg2);
    }

    @Override
    public void error(final String format, final Object... args) {
        require(ERROR);
        super.error(format, args);
    }

    @Override
    public void error(final String msg, final Throwable t) {
        require(ERROR);
        super.error(msg, t);
    }

    @Override
    public void error(final Marker marker, final String msg) {
        require(ERROR);
        super.error(marker, msg);
    }

    @Override
    public void error(final Marker marker, final String format, final Object arg) {
        require(ERROR);
        super.error(marker, format, arg);
    }

    @Override
    public void error(final Marker marker, final String format, final Object arg1,
            final Object arg2) {
        require(ERROR);
        super.error(marker, format, arg1, arg2);
    }

    @Override
    public void error(final Marker marker, final String format, final Object... args) {
        require(ERROR);
        super.error(marker, format, args);
    }

    @Override
    public void error(final Marker marker, final String msg, final Throwable t) {
        require(ERROR);
        super.error(marker, msg, t);
    }

    private void require(final Level current) {
        if (!current.isGreaterOrEqual(minimum))
            throw new IllegalStateException(
                    format("%s logging disabled for logger \"%s\" [%s]", current, getName(),
                            getClass().getName()));
    }

    public static void main(final String... args) {
        try {
            final MinimumLogger main = new MinimumLogger(LoggerFactory.getLogger("main"), INFO);
            main.info("Just fine");
            main.trace("Too trivial!");
        } catch (final IllegalStateException e) {
            e.printStackTrace(out);
        }
    }
}
