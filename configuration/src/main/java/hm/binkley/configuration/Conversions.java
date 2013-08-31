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
    private Conversions() {
    }

    public static Function<Exception, RuntimeException> unchecked() {
        return Unchecked.UNCHECKED;
    }

    public static Function<String, String> strings() {
        return identity();
    }

    private static final class Unchecked
            implements Function<Exception, RuntimeException> {
        private static final Unchecked UNCHECKED = new Unchecked();

        @Nonnull
        @Override
        public RuntimeException apply(final Exception input) {
            return new RuntimeException(input);
        }

        @Nonnull
        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }
}
