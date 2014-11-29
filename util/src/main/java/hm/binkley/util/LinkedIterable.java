package hm.binkley.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.NONNULL;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterator.SORTED;
import static java.util.Spliterators.spliteratorUnknownSize;

/**
 * {@code LinkedIterable} is a read-only iterable facade over a linked series of values defined by a
 * head value and traversal and termination functions.  The functions should not structurally modify
 * any underlying objects: repeated iterations or stream traversal should produce the same values in
 * the same order; best if they are pure functions.
 *
 * @param <T> the value type
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public final class LinkedIterable<T>
        implements Iterable<T> {
    /** The characteristics for spliterators. */
    public static final int SPLITERATOR_CHARACTERISTICS = IMMUTABLE | NONNULL | ORDERED | SORTED;

    private final T head;
    private final Function<T, T> traverse;
    private final Predicate<T> terminate;

    /**
     * Creates a new {@code LinkedIterable} for the given parameters.  If <var>head</var> terminates
     * the iterable is empty.
     *
     * @param <T> the value type
     * @param head the optional head value
     * @param terminate the termination function, never missing
     * @param traverse the traversal function, never missing
     *
     * @return the new {@code LinkedIterable}, never missing
     */
    @Nonnull
    public static <T> Iterable<T> over(@Nullable final T head,
            @Nonnull final Predicate<T> terminate, @Nonnull final Function<T, T> traverse) {
        return new LinkedIterable<>(head, terminate, traverse);
    }

    /**
     * Creates a new {@code LinkedIterable} for the given parameters.  If <var>head</var> terminates
     * the iterable is empty.  Supplies an initial head value equivalent to {@code
     * traverse.apply(null)}.
     *
     * @param <T> the value type
     * @param terminate the termination function, never missing
     * @param traverse the traversal function, never missing
     *
     * @return the new {@code LinkedIterable}, never missing
     */
    @Nonnull
    public static <T> Iterable<T> over(@Nonnull final Predicate<T> terminate,
            @Nonnull final Function<T, T> traverse) {
        return new LinkedIterable<>(traverse.apply(null), terminate, traverse);
    }

    private LinkedIterable(final T head, final Predicate<T> terminate,
            final Function<T, T> traverse) {
        this.head = head;
        this.terminate = terminate;
        this.traverse = traverse;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private T last = head;

            @Override
            public boolean hasNext() {
                return !terminate.test(last);
            }

            @Override
            public T next() {
                final T next = last;
                last = traverse.apply(last);
                return next;
            }
        };
    }

    @Override
    public Spliterator<T> spliterator() {
        return spliteratorUnknownSize(iterator(), SPLITERATOR_CHARACTERISTICS);
    }
}
