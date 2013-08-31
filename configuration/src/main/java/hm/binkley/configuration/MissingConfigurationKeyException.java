package hm.binkley.configuration;

/**
 * {@code MissingConfigurationKeyException} is a standard exception for configurations to throw when
 * a required configuration key is missing.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public class MissingConfigurationKeyException
        extends RuntimeException {
    /**
     * Constructs a new {@code MissingConfigurationKeyException} for the given <var>message</var>.
     *
     * @param message the exception message
     */
    public MissingConfigurationKeyException(final String message) {
        super(message);
    }

    /**
     * Constructs a new {@code MissingConfigurationKeyException} for the given <var>cause</var>.
     *
     * @param cause the underlying exception
     */
    public MissingConfigurationKeyException(final Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new {@code MissingConfigurationKeyException} for the given <var>message</var>
     * and <var>cause</var>.
     *
     * @param message the exception message
     * @param cause the underlying exception
     */
    public MissingConfigurationKeyException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
