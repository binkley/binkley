package hm.binkley.util;

import javax.annotation.Nonnull;

/**
 * {@code Xnum} needs documentation.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public abstract class Xnum<X extends Xnum<X>>
        implements Comparable<X> {
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

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public final boolean equals(final Object obj) {
        return this == obj;
    }

    @Override
    protected final Object clone()
            throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    @Override
    public String toString() {
        return name;
    }

    protected final void finalize() {
    }

    public final int compareTo(final X o) {
        final Xnum that = (Xnum) o;
        if (getClass() != that.getClass() && // optimization
                getDeclaringClass() != that.getDeclaringClass())
            throw new ClassCastException();
        return this.ordinal - that.ordinal;
    }

    @SuppressWarnings("unchecked")
    public final Class<X> getDeclaringClass() {
        final Class clazz = getClass();
        final Class zuper = clazz.getSuperclass();
        return (zuper == Xnum.class) ? clazz : zuper;
    }
}
