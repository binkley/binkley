package hm.binkley.annotation.processing.y;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * {@code YProperty} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public final class YProperty {
    @Nonnull
    public final String name;
    @Nullable
    public final Object value;

    YProperty(final Map.Entry<String, Object> raw) {
        name = raw.getKey();
        value = raw.getValue();
    }
}
