/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util;

import com.google.common.base.Predicate;
import hm.binkley.util.SingleTyped.Typed;

import javax.annotation.Nonnull;
import java.util.List;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.filter;

/**
 * {@code SingleTyped} needs documentation.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
final class SingleTyped<T, X extends Xnum<X> & Typed<T>>
        implements Predicate<X> {
    private final Class<T> type;

    SingleTyped(final Class<T> type) {
        this.type = type;
    }

    @SuppressWarnings("unchecked")
    static <T, V extends Xnum, X extends Xnum<X> & Typed<T>> X coerceOneOf(final Class<V> xnumType,
            final List<V> values, final String name) {
        return (X) Xnum.valueOf(xnumType, values, name);
    }

    @SuppressWarnings("unchecked")
    static <T, V extends Xnum, X extends Xnum<X> & Typed<T>> List<X> allOf(final List<V> values,
            final Class<T> type) {
        // Type coersion required; safe as we filter on T
        return copyOf(filter((List<X>) values, new SingleTyped<T, X>(type)));
    }

    @Override
    public boolean apply(final X xnum) {
        return type == xnum.type();
    }

    /** {@code Typed} needs documentation. */
    public static interface Typed<T> {
        @Nonnull
        Class<T> type();
    }
}
