/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util.logging.osi;

import ch.qos.logback.classic.Level;
import hm.binkley.util.logging.MarkedLogger;
import hm.binkley.util.logging.MinimumLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import javax.annotation.Nonnull;

import static ch.qos.logback.classic.Level.ALL;
import static ch.qos.logback.classic.Level.INFO;
import static ch.qos.logback.classic.Level.WARN;
import static org.slf4j.MarkerFactory.getMarker;

/**
 * {@code SupportLoggers} are custom {@link MarkedLogger}s for Ops Support and the business. Methods
 * create new marked loggers with the enum names as markers. <p> There is one factory method variant
 * for each style of logger creation: <dl> <dt>{@link #getLogger(Class)}</dt> <dd>Creates a new
 * underlying logger from a class</dd> <dt>{@link #getLogger(String)}</dt> <dd>Creates a new
 * underlying logger from a logger name</dd> <dt>{@link #getLogger(Logger)}</dt> <dd>Reuses an
 * existing underlying logger</dd> </dl> <p> Applications should configure logback or other logging
 * system by marker: <dl><dt>{@link #ALERT}</dt> <dd>Send alerts to Ops Support</dd> <dt>{@link
 * #APPLICATION}</dt> <dd>Logs normally following logback configuration</dd> <dt>{@link #AUDIT}</dt>
 * <dd>Records an audit trail for the business, typically to a database</dd></dl>
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public enum SupportLoggers {
    /**
     * Marks "ALERT" loggers to send alerts to Ops Support.  Rejects logging at less than {@code
     * WARN} level (throws {@code IllegalStateException}).
     */
    ALERT(WARN),
    /** Unmarked loggers for normal logging. */
    APPLICATION(ALL) {
        @Nonnull
        @Override
        public Logger getLogger(@Nonnull final Class<?> logger) {
            return LoggerFactory.getLogger(logger);
        }

        @Nonnull
        @Override
        public Logger getLogger(@Nonnull final String logger) {
            return LoggerFactory.getLogger(logger);
        }

        @Nonnull
        @Override
        public Logger getLogger(@Nonnull final Logger logger) {
            return logger;
        }
    },
    /** Marks "AUDIT" loggers to record an audit trail for the business, typically to a database. */
    AUDIT(INFO),
    /** Trace loggers ({@link XLogger} for debugging. */
    TRACE(ALL) {
        @Nonnull
        @Override
        public XLogger getLogger(@Nonnull final Class<?> logger) {
            return XLoggerFactory.getXLogger(logger);
        }

        @Nonnull
        @Override
        public XLogger getLogger(@Nonnull final String logger) {
            return XLoggerFactory.getXLogger(logger);
        }

        @Nonnull
        @Override
        public XLogger getLogger(@Nonnull final Logger logger) {
            return (XLogger) logger;
        }
    };
    @Nonnull
    private final Level minimum;

    SupportLoggers(@Nonnull final Level minimum) {
        this.minimum = minimum;
    }

    /**
     * Redundant method for {@link #TRACE} returning {@code XLogger}.
     *
     * @param logger the logger class, never missing
     *
     * @return the XLogger, never missing
     *
     * @todo Java needs anonymous instance covariant returns to avoid casting
     */
    @Nonnull
    public static XLogger trace(@Nonnull final Class<?> logger) {
        return trace(logger.getName());
    }

    /**
     * Redundant method for {@link #TRACE} returning {@code XLogger}.
     *
     * @param logger the logger name, never missing
     *
     * @return the XLogger, never missing
     *
     * @todo Java needs anonymous instance covariant returns to avoid casting
     */
    @Nonnull
    public static XLogger trace(@Nonnull final String logger) {
        return (XLogger) TRACE.getLogger(logger);
    }

    /**
     * Creates a new marked logger for the given <var>logger</var>.
     *
     * @param logger the underlying class, never missing
     *
     * @return the wrapping marked logger, never missing
     */
    @Nonnull
    public Logger getLogger(@Nonnull final Class<?> logger) {
        return getLogger(LoggerFactory.getLogger(logger));
    }

    /**
     * Creates a new marked logger for the given <var>logger</var>.
     *
     * @param logger the underlying logger name, never missing
     *
     * @return the wrapping marked logger, never missing
     */
    @Nonnull
    public Logger getLogger(@Nonnull final String logger) {
        return getLogger(LoggerFactory.getLogger(logger));
    }

    /**
     * Creates a new marked logger for the given <var>logger</var>.
     *
     * @param logger the underlying SLF4j logger, never missing
     *
     * @return the wrapping marked logger, never missing
     */
    @Nonnull
    public Logger getLogger(@Nonnull final Logger logger) {
        return ALL == minimum ? new MarkedLogger(logger, getMarker(name())) : new MinimumLogger(
                new MarkedLogger(logger, MinimumLogger.class.getName(), getMarker(name())),
                minimum);
    }
}
