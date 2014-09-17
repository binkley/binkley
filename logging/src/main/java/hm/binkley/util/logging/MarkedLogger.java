/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util.logging;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.ext.LoggerWrapper;

import javax.annotation.Nonnull;

/**
 * {@code MarkedLogger} is an SLF4J logger with a default marker applied to all methods with a
 * marker variant.
 * <p/>
 * Example: <pre>
 *     final Marker marker = MarkerFactory.getMarker("AUDIT");
 *     final Logger logger = new MarkedLogger(LoggerFactory.getLogger(getClass()), marker);
 *     logger.error("Missing counterparty details on order: {}", order);
 *     logger.error(marker, "Missing counterparty details on order: {}", order);
 * </pre>
 * Marks this logging event as "AUDIT".  Suitable logger configuration might redirect this event to
 * Remedy.  Both logging lines are equivalent.
 * <p/>
 * In Logback configuration use {@code %marker} to print the marker in the encoder pattern.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public class MarkedLogger
        extends LoggerWrapper {
    private final Marker marker;

    /**
     * Gets the underlying wrapped logger for extensions.
     *
     * @return the underlying logger, never missing
     */
    @Nonnull
    public final Logger getUnderlying() {
        return logger;
    }

    /**
     * Constructds a new {@code MarkedLogger} for the given parameters.
     *
     * @param logger the logger to delegate to, never missing
     * @param marker the marker for unmarked logging, never missing
     */
    public MarkedLogger(@Nonnull final Logger logger, @Nonnull final Marker marker) {
        this(logger, MarkedLogger.class.getName(), marker);
    }

    /**
     * Constructds a new {@code MarkedLogger} for the given parameters suitable as a base class.
     *
     * @param logger the logger to delegate to, never missing
     * @param fqcn the fully-qualified class name of the extending logger, never missing
     * @param marker the marker for unmarked logging, never missing
     */
    public MarkedLogger(@Nonnull final Logger logger, @Nonnull final String fqcn,
            @Nonnull final Marker marker) {
        super(logger, fqcn);
        this.marker = marker;
    }

    @Override
    public void trace(final String msg) {
        trace(marker, msg);
    }

    @Override
    public void trace(final String format, final Object arg) {
        trace(marker, format, arg);
    }

    @Override
    public void trace(final String format, final Object arg1, final Object arg2) {
        trace(marker, format, arg1, arg2);
    }

    @Override
    public void trace(final String format, final Object... args) {
        trace(marker, format, args);
    }

    @Override
    public void trace(final String msg, final Throwable t) {
        trace(marker, msg, t);
    }

    @Override
    public void debug(final String msg) {
        debug(marker, msg);
    }

    @Override
    public void debug(final String format, final Object arg) {
        debug(marker, format, arg);
    }

    @Override
    public void debug(final String format, final Object arg1, final Object arg2) {
        debug(marker, format, arg1, arg2);
    }

    @Override
    public void debug(final String format, final Object... args) {
        debug(marker, format, args);
    }

    @Override
    public void debug(final String msg, final Throwable t) {
        debug(marker, msg, t);
    }

    @Override
    public void info(final String msg) {
        info(marker, msg);
    }

    @Override
    public void info(final String format, final Object arg) {
        info(marker, format, arg);
    }

    @Override
    public void info(final String format, final Object arg1, final Object arg2) {
        info(marker, format, arg1, arg2);
    }

    @Override
    public void info(final String format, final Object... args) {
        info(marker, format, args);
    }

    @Override
    public void info(final String msg, final Throwable t) {
        info(marker, msg, t);
    }

    @Override
    public void warn(final String msg) {
        warn(marker, msg);
    }

    @Override
    public void warn(final String format, final Object arg) {
        warn(marker, format, arg);
    }

    @Override
    public void warn(final String format, final Object arg1, final Object arg2) {
        warn(marker, format, arg1, arg2);
    }

    @Override
    public void warn(final String format, final Object... args) {
        warn(marker, format, args);
    }

    @Override
    public void warn(final String msg, final Throwable t) {
        warn(marker, msg, t);
    }

    @Override
    public void error(final String msg) {
        error(marker, msg);
    }

    @Override
    public void error(final String format, final Object arg) {
        error(marker, format, arg);
    }

    @Override
    public void error(final String format, final Object arg1, final Object arg2) {
        error(marker, format, arg1, arg2);
    }

    @Override
    public void error(final String format, final Object... args) {
        error(marker, format, args);
    }

    @Override
    public void error(final String msg, final Throwable t) {
        error(marker, msg, t);
    }
}
