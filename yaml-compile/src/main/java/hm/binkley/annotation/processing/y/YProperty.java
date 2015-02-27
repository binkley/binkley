package hm.binkley.annotation.processing.y;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * {@code YProperty} models properties for methods.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public final class YProperty {
    /** The property name, never missing. */
    @Nonnull
    public final String name;
    /** The optional property value. */
    @Nullable
    public final Object value;

    YProperty(final Map.Entry<String, Object> raw) {
        name = raw.getKey();
        value = raw.getValue();
    }

    /**
     * Convenience for method reference.
     *
     * @return {@link #name}
     */
    public String name() {
        return name;
    }

    /**
     * Convenience for method reference.
     *
     * @return {@link #value}
     */
    public Object value() {
        return value;
    }
}
