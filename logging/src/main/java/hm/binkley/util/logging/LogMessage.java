/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util.logging;

import com.google.common.annotations.Beta;
import com.google.inject.assistedinject.Assisted;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.ResourceBundle;

import static java.text.MessageFormat.format;

/**
 * {@code LogMessage} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
@Beta
public final class LogMessage {
    private final Level level;
    private final String message;
    private final Object[] parameters;

    // TODO: Should IntelliJ warn for varargs parameters?
    @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
    @Inject
    public LogMessage(final ResourceBundle bundle, @Assisted final String key,
            @Assisted final Object... parameters) {
        this.parameters = parameters;
        level = Level.valueOf(bundle.getString(key + ".level"));
        message = bundle.getString(key + ".message");
    }

    public void logTo(final Logger logger) {
        logTo(logger, null, parameters);
    }

    public void logTo(final Logger logger, final Object... parameters) {
        logTo(logger, null, parameters);
    }

    public void logTo(final Logger logger, final Throwable cause) {
        logTo(logger, cause, parameters);
    }

    public void logTo(final Logger logger, final Throwable cause, final Object... parameters) {
        level().log(logger, format(message(), parameters), cause);
    }

    public Level level() {
        return level;
    }

    public String message() {
        return message;
    }

    @Override
    public String toString() {
        return format(message, parameters);
    }
}
