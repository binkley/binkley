package hm.binkley.util;

import java.util.NoSuchElementException;
import java.util.Random;

/**
 * {@code NonRandom} provides deterministic random numbers for testing
 * specifically for {@link #nextInt()} and {@link #nextInt(int)}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public class NonRandom
        extends Random {
    private final int[] nexts;
    private int i;

    /**
     * {@code NonRandom} provides the given sequence of {@code int}s to {@link
     * #next(int)}.
     *
     * @param nexts the sequence of values to return
     */
    public NonRandom(final int... nexts) {
        this.nexts = nexts;
    }

    @Override
    public int nextInt() {
        return nextInt(Integer.MAX_VALUE);
    }

    /**
     * Provides the next available element from {@link #nexts}.
     *
     * @return the next {@code int}
     *
     * @throws NoSuchElementException if {@link #nexts} is exhausted.
     */
    @Override
    public int nextInt(final int bound) {
        if (i == nexts.length)
            throw new NoSuchElementException("Exhausted random numbers");
        final int next = nexts[i++];
        if (next > bound)
            throw new IllegalArgumentException("Bound less than nexts");
        return next;
    }
}
