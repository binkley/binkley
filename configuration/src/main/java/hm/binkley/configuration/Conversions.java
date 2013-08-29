package hm.binkley.configuration;

import com.google.common.base.Function;

import javax.annotation.Nonnull;

import static com.google.common.base.Functions.identity;

/**
 * {@code Conversions} needs documentation.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public final class Conversions {
    public static final Unchecked UNCHECKED = new Unchecked();

    private Conversions() {
    }

    public static Function<Exception, RuntimeException> unchecked() {
        return UNCHECKED;
    }

    public static Function<String, String> strings() {
        return identity();
    }

    private static class Unchecked
            implements Function<Exception, RuntimeException> {
        @Nonnull
        @Override
        public RuntimeException apply(final Exception input) {
            return new RuntimeException(input);
        }
    }
}
