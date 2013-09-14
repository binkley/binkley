package hm.binkley.util;

import javax.annotation.Nonnull;

/**
 * {@code Xnum} needs documentation.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public abstract class Xnum<X extends Xnum<X>> {
    private final String name;
    private final int ordinal;

    protected Xnum(@Nonnull final String name, final int ordinal) {
        this.name = name;
        this.ordinal = ordinal;
    }

    @Nonnull
    public final String name() {
        return name;
    }

    public final int ordinal() {
        return ordinal;
    }
}
