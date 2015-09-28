package lab.dynafig.spring;

import lab.dynafig.DefaultDynafig;
import org.springframework.core.env.Environment;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static java.util.Optional.ofNullable;

/**
 * {@code SpringDynafig} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public class SpringDynafig
        extends DefaultDynafig {
    public SpringDynafig(final Environment env) {
        super(key -> env.containsProperty(key) ? ofNullable(
                env.getProperty(key)) : null);
    }

    @Override
    public void update(@Nonnull final String key,
            @Nullable final String value) {
        track(key); // Lazy init from Spring
        super.update(key, value);
    }
}
