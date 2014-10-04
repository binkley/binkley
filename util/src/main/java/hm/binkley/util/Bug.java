package hm.binkley.util;

import static java.lang.String.format;

/**
 * {@code Bug} represents "impossible" coding mistakes or conditions.  It is more expressive than
 * {@code IllegalArgumentException} or {@code IllegalStateException}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public class Bug
        extends IllegalStateException {
    /**
     * Constructs a new {@code Bug} with the given parameters.
     *
     * @param message the formatted message, never missing
     * @param args any message format parameters
     *
     * @see String#format(String, Object...)
     */
    public Bug(final String message, final Object... args) {
        this(null, message, args);
    }

    /**
     * Constructs a new {@code Bug} with the given parameters.
     *
     * @param cause the root cause wrapped in this bug
     * @param message the formatted message, never missing
     * @param args any message format parameters
     *
     * @see String#format(String, Object...)
     */
    public Bug(final Throwable cause, final String message, final Object... args) {
        super("BUG: " + format(message, args), cause);
    }
}
