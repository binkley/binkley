package hm.binkley.util;

import javax.annotation.Nonnull;
import java.lang.reflect.ParameterizedType;

import static java.lang.String.format;

/**
 * {@code Xnum} ("extensible enum") shadows {@link Enum} permitting generic subclassing.
 * Construction of usable instances is identical to {@code enum} (called xnum here to distinguish):
 * <ol><li>Extend {@code Xnum} with an abstract base class for the xnum type.</li> <li>Implement
 * instances of the xnum base class as required as class constants.</li> <li>Implement a {@code
 * values()} class method to return the xnum instances in declartion order.</li> <li>Implement a
 * {@code valueOf(String)} class method to lookup xnum instances by name.  See {@link
 * #valueOf(Class, Iterable, String)} helper.</li></ol> Afterwards class hierarchy should be:
 * <blockquote>Xnum &lt;- Base class &lt;- Members</blockquote> The generic type information should
 * reside on the base class, specialized by the members.  If you do not need specialization type
 * generic type, use {@code enum} not this class.
 *
 * @param <X> the extending enum type
 *
 * @author <a href="mailto:Brian.Oxley@Macquarie.COM">Brian Oxley</a>
 * @todo Implement {@code XnumMap} and {@code XnumSet}
 * @see hm.binkley.util.EgXnum Example xnum implementation
 */
public abstract class Xnum<X extends Xnum<X>>
        implements Comparable<X> {
    private final String name;
    private final int ordinal;

    protected Xnum(@Nonnull final String name, final int ordinal) {
        this.name = name;
        this.ordinal = ordinal;
    }

    /** Helper for implementing {@code valueOf(String)}. */
    @Nonnull
    protected static <X extends Xnum<X>> X valueOf(final Class<X> type,
            final Iterable<? extends X> values, final String name) {
        for (final X xnum : values)
            if (xnum.name().equals(name))
                return xnum;
        if (null == name)
            throw new NullPointerException("Missing name");
        throw new IllegalArgumentException(format("No xnum of type %s for name: %s", type, name));
    }

    /**
     * Helper to find type parameter of xnum instance.
     *
     * @param parameter the 0-based index of types
     * @param <T> the type parameters
     *
     * @return the parameter type token, never missing
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    protected <T> Class<T> typeOf(final int parameter) {
        return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[parameter];
    }

    /**
     * Compares this xnum with the specified object for order.  Returns a negative integer, zero, or
     * a positive integer as this object is less than, equal to, or greater than the specified
     * object.
     * <p/>
     * Xnum constants are only comparable to other xnum constants of the same xnum type.  The
     * natural order implemented by this method is the order in which the constants are declared.
     */
    @Override
    public final int compareTo(final X that) {
        return Integer.valueOf(ordinal()).compareTo(that.ordinal());
    }

    /**
     * Returns the Class object corresponding to this enum constant's enum type.  Two xnum
     * constants e1 and  e2 are of the same xnum type if and only if: <pre>
     * e1.getDeclaringClass() == e2.getDeclaringClass()</pre>
     * (The value returned by this method may differ from the one returned by the {@link
     * Object#getClass} method for xnum constants with constant-specific class bodies.)
     *
     * @return the Class object corresponding to this enum constant's xnum type
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public final Class<X> getDeclaringClass() {
        final Class clazz = getClass();
        final Class zuper = clazz.getSuperclass();
        return Xnum.class == zuper ? clazz : zuper;
    }

    /**
     * Returns the name of this xnum constant, exactly as declared in its xnum declaration.
     * <p/>
     * <b>Most programmers should use the {@link #toString} method in preference to this one, as the
     * toString method may return a more user-friendly name.</b>  This method is designed primarily
     * for use in specialized situations where correctness depends on getting the exact name, which
     * will not vary from release to release.
     *
     * @return the name of this xnum constant
     */
    @Nonnull
    public final String name() {
        return name;
    }

    /**
     * Returns the ordinal of this extended enumeration constant (its position in its xnum
     * declaration, where the initial constant is assigned an ordinal of zero).
     * <p/>
     * Most programmers will have no use for this method.  It is designed for use by sophisticated
     * xnum-based data structures, such as {@link XnumSet} and {@link XnumMap}.
     *
     * @return the ordinal of this extended enumeration constant
     */
    public final int ordinal() {
        return ordinal;
    }

    /**
     * Returns a hash code for this xnum constant.
     *
     * @return a hash code for this xnum constant.
     */
    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    /**
     * Returns true if the specified object is equal to this xnum constant.
     *
     * @param o the object to be compared for equality with this object.
     *
     * @return true if the specified object is equal to this xnum constant.
     */
    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public final boolean equals(final Object o) {
        return this == o;
    }

    /**
     * Throws {@code CloneNotSupportedException}.  This guarantees that xnums are never cloned,
     * which is necessary to preserve their "singleton" status.
     *
     * @throws CloneNotSupportedException always
     */
    @Override
    protected final Object clone()
            throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * Returns the name of this xnum constant, as contained in the declaration.  This method may be
     * overridden, though it typically isn't necessary or desirable.  An xnum type should override
     * this method when a more "programmer-friendly" string form exists.
     *
     * @return the name of this xnum constant
     */
    @Override
    public String toString() {
        return name;
    }

    /** Xnum classes cannot have finalize methods. */
    @Override
    protected final void finalize() {
    }
}
