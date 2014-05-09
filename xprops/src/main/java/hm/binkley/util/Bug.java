package hm.binkley.util;

import static java.lang.String.format;

/**
 * {@code Bug} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public class Bug
        extends IllegalStateException {
    public Bug(final String message, final Object... args) {
        this(null, message, args);
    }

    public Bug(final Throwable cause, final String message, final Object... args) {
        super("BUG: " + format(message, args), cause);
    }
}
