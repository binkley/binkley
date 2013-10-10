package hm.binkley.util.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import javax.annotation.Nonnull;

import static org.slf4j.MarkerFactory.getMarker;

/**
 * {@code SupportLoggers} are custom {@link MarkedLogger}s for support. Methods create new marked
 * loggers with the enum names as markers.
 * <p/>
 * There is one factory method variant for each style of logger creation: <dl> <dt>{@link
 * #getLogger(Class)}</dt> <dd>Creates a new underlying logger from a class</dd> <dt>{@link
 * #getLogger(String)}</dt> <dd>Creates a new underlying logger from a logger name</dd> <dt>{@link
 * #getLogger(Logger)}</dt> <dd>Reuses an existing underlying logger</dd> </dl>
 * <p/>
 * Programs should configure logback or other logging system by marker: <dl><dt>{@link #ALERT}</dt>
 * <dd>Send alerts to a human being</dd> <dt>{@link #APPLICATION}</dt> <dd>Logs normally following
 * logback configuration</dd> <dt>{@link #AUDIT}</dt> <dd>Records an audit trail, typically to a
 * database</dd></dl>
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public enum SupportLoggers {
    /** Marks "ALERT" loggers to send alerts to a human being. */
    ALERT,
    /** Unmarked loggers for normal logging. */
    APPLICATION {
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
    /** Marks "AUDIT" loggers to record an audit trail, typically to a database. */
    AUDIT,
    /** Trace loggers ({@link XLogger} for debugging. */
    TRACE {
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
        return (XLogger) TRACE.getLogger(logger);
    }

    public static void main(final String... args) {
        final Logger alert = ALERT.getLogger(SupportLoggers.class);
        final Logger audit = AUDIT.getLogger(SupportLoggers.class);
        final Logger application = APPLICATION.getLogger(SupportLoggers.class);
        final XLogger trace = trace(SupportLoggers.class);

        trace.entry(args);
        try {
            alert.error("Ouch, {}!", "Robin");
            audit.error("Ouch, {}!", "Batman");
            audit.trace("Ouch, {}!", "Joker");
            application.info("Ouch, {}!", "Batgirl");
            trace.exit();
        } catch (final Throwable t) {
            trace.catching(t);
        }
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
        return new MarkedLogger(getMarker(name()), LoggerFactory.getLogger(logger),
                getClass().getName());
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
        return new MarkedLogger(getMarker(name()), LoggerFactory.getLogger(logger),
                getClass().getName());
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
        return new MarkedLogger(getMarker(name()), logger, getClass().getName());
    }
}
