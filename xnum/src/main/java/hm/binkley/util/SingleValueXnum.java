/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import java.util.List;

import static hm.binkley.util.SingleTyped.Typed;
import static hm.binkley.util.SingleTyped.allOf;

/**
 * {@code SingleValueXnum} is a template for {@link TODO} to generate single-valued xnums with an
 * annotation processor.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Annotation processor adds xnum constants
 * @todo Annotation processor fills in VALUES
 * @todo Annotation processor adds ordinal constants
 * @todo Annotation processor writes out implementation classes
 * @see EgXnum
 */
public abstract class SingleValueXnum<T>
        extends Xnum<SingleValueXnum<T>>
        implements Typed<T> {
    //    public static final SingleValueXnum<Integer> FOO = new FOO();
    private static final List<? extends SingleValueXnum<?>> VALUES = ImmutableList
            .<SingleValueXnum<?>>of();

    /**
     * Gets an unmodifiable list of xnum values in declaration order.
     *
     * @return all xnum values, never missing
     */
    @Nonnull
    public static List<? extends SingleValueXnum<?>> values() {
        return VALUES;
    }

    /**
     * Finds {@code EgXnum} instances by their specialization type.
     *
     * @param type the specialization type token, never missing
     * @param <T> the specialization type
     *
     * @return the sequence of instances of <var>type</var>, never missing
     */
    @Nonnull
    public static <T> List<SingleValueXnum<T>> valuesOfType(@Nonnull final Class<T> type) {
        return allOf(values(), type);
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
    public static SingleValueXnum<?> valueOf(@Nonnull final String name) {
        return valueOf(SingleValueXnum.class, values(), name);
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
     */
    @Nonnull
    public static <T> SingleValueXnum<T> valueOf(@Nonnull final String name,
            @Nonnull final Class<T> parameterType) {
        return valueOf(SingleValueXnum.class, values(), name, 0, parameterType);
        //        return coerceOneOf(SingleValueXnum.class, values(), name, parameterType);
    }

    private SingleValueXnum(@Nonnull final String name, final int ordinal) {
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
        //        FOO
        ;

        @Nonnull
        @SuppressWarnings("unchecked")
        public <T> SingleValueXnum<T> xnum() {
            return (SingleValueXnum<T>) VALUES.get(ordinal());
        }
    }
}
