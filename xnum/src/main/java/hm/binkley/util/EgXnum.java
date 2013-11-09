/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import java.util.List;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.filter;
import static hm.binkley.util.SingleTyped.Typed;
import static java.lang.String.format;
import static java.lang.System.out;

/**
 * {@code EgXnum} is a sample {@link Xnum}, not a prodution class.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public abstract class EgXnum<T>
        extends Xnum<EgXnum<T>>
        implements Typed<T> {
    public static final EgXnum<Integer> FOO = new FOO();
    public static final EgXnum<String> BAR = new BAR();
    public static final EgXnum<String> BAZ = new BAZ();
    private static final List<EgXnum<?>> VALUES = ImmutableList.<EgXnum<?>>of(FOO, BAR, BAZ);

    /**
     * Gets an unmodifiable list of xnum values in declaration order.
     *
     * @return all xnum values, never missing
     */
    @Nonnull
    public static List<EgXnum<?>> values() {
        return VALUES;
    }

    /**
     * Finds {@code EgXnum} instances by their specialization type.
     *
     * @param type the specialization type token, never missing
     * @param <T> the specialization type
     *
     * @return the sequence of instances of <var>type</var>, never missing
     *
     * @see SingleTyped#allOf(List, Class)
     */
    @Nonnull
    public static <T> List<EgXnum<T>> valuesOfType(@Nonnull final Class<T> type) {
        return copyOf(
                filter((List<EgXnum<T>>) (List) values(), new SingleTyped<T, EgXnum<T>>(type)));
    }

    /**
     * Returns the xnum constant with the specified <var>name</var>.  The name must match exactly an
     * identifier used to declare an xnum constant in this type.  The value type is unknown.
     *
     * @param name the xnum name, never missing
     *
     * @return the xnum, never missing
     *
     * @throws IllegalArgumentException if the specified xnum type has no constant with the
     * specified name
     * @throws NullPointerException if <var>name</var> is null
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public static EgXnum<?> valueOf(@Nonnull final String name) {
        return valueOf(EgXnum.class, values(), name);
    }

    /**
     * Returns the xnum constant of the specified xnum type with the specified <var>name</var>.  The
     * type of the xnum constant must be assignable to <var>class</var>.  The name must match
     * exactly an identifier used to declare an xnum constant in this type.
     *
     * @param name the xnum name, never missing
     * @param parameterType the xnum type token, never missing
     *
     * @return the xnum, never missing
     *
     * @throws IllegalArgumentException if the specified xnum type has no constant with the
     * specified name
     * @throws NullPointerException if <var>name</var> is null
     * @throws ClassCastException if <var>type</var> is unassignable from using the xnum constant
     * @see SingleTyped#coerceOneOf(Class, List, String, Class)
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public static <T> EgXnum<T> valueOf(@Nonnull final String name,
            @Nonnull final Class<T> parameterType) {
        return (EgXnum<T>) valueOf(EgXnum.class, values(), name, 0, parameterType);
    }

    public static void main(final String... args) {
        out.println(EgXnum.valueOf("BAR"));
        out.println(EgXnum.valueOf("BAR", String.class));

        for (final EgXnum<?> xnum : EgXnum.values())
            out.println(
                    format("%s(%d)[%s] = %s - %s / %s", xnum.name(), xnum.ordinal(), xnum.type(),
                            xnum.get(), xnum.getClass(), xnum.getDeclaringClass()));

        for (final EgXnum<String> xnum : EgXnum.valuesOfType(String.class))
            out.println(format("%s = %s", xnum, xnum.get()));

        final EgXnum.Ordinal ordinal = EgXnum.Ordinal.BAR;
        final EgXnum<?> xnum = ordinal.xnum();
        out.println("xnum.get = " + xnum.get());
    }

    private EgXnum(@Nonnull final String name, final int ordinal) {
        super(name, ordinal);
    }

    /**
     * Gets the typed value held by the instance.  This is the entire point of xnums; enums do not
     * support covariant return as they are instances of anonymous subclasses.
     *
     * @return the instance value
     */
    public abstract T get();

    @Nonnull
    @Override
    public final Class<T> type() {
        return typeOf(0);
    }

    public enum Ordinal {
        FOO, BAR, BAZ;

        @Nonnull
        @SuppressWarnings("unchecked")
        public <T> EgXnum<T> xnum() {
            return (EgXnum<T>) VALUES.get(ordinal());
        }
    }

    private static final class FOO
            extends EgXnum<Integer> {
        private FOO() {
            super("FOO", Ordinal.FOO.ordinal());
        }

        @Override
        public Integer get() {
            return 13;
        }
    }

    private static final class BAR
            extends EgXnum<String> {
        private BAR() {
            super("BAR", Ordinal.BAR.ordinal());
        }

        @Override
        public String get() {
            return "Friday";
        }
    }

    private static final class BAZ
            extends EgXnum<String> {
        private BAZ() {
            super("BAZ", Ordinal.BAR.ordinal());
        }

        @Override
        public String get() {
            return "Lucky!";
        }
    }
}
