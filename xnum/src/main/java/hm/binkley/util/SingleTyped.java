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
import static hm.binkley.util.Xnum.valueOf;

/**
 * {@code SingleTyped} needs documentation.
 *
 * @param <V> the value type
 * @param <X> the extending xnum type
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
final class SingleTyped<V, X extends Xnum<X> & Typed<V>>
        implements Predicate<X> {
    private final Class<V> type;

    SingleTyped(final Class<V> type) {
        this.type = type;
    }

    @SuppressWarnings("unchecked")
    static <T, V extends Xnum, X extends Xnum<X> & Typed<T>> X coerceOneOf(final Class<V> xnumType,
            final List<? extends V> values, final String name, final Class<T> valueType) {
        return (X) valueOf(xnumType, values, name, 0, valueType);
    }

    @SuppressWarnings("unchecked")
    static <T, V extends Xnum, X extends Xnum<X> & Typed<T>> List<X> allOf(
            final List<? extends V> values, final Class<T> type) {
        // Type coersion required; safe as we filter on T
        return copyOf(filter((List<X>) values, new SingleTyped<T, X>(type)));
    }

    @Override
    public boolean apply(final X xnum) {
        return type.isAssignableFrom(xnum.type());
    }

    /**
     * {@code Typed} needs documentation.
     *
     * @param <V> the value type
     */
    public static interface Typed<V> {
        @Nonnull
        Class<V> type();
    }
}
