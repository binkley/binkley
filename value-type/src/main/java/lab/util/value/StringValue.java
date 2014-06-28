package lab.util.value;

import javax.annotation.Nonnull;

/**
 * {@code StringValue} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public abstract class StringValue<V extends StringValue<V>>
        extends ComparableValue<String, V> {
    protected StringValue(@Nonnull final String value) {
        super(value);
    }

    @Nonnull
    @Override
    public final Class<String> type() {
        return String.class;
    }
}
