package hm.binkley.util;

import java.util.NoSuchElementException;
import java.util.Random;

/**
 * {@code NonRandom} provides deterministic random numbers for testing.
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

    /**
     * Provides the next available element from {@link #nexts}.
     *
     * @return the next {@code int}
     *
     * @throws NoSuchElementException if {@link #nexts} is exhausted.
     * @todo More clever way to handle bits == 32?
     */
    @Override
    protected int next(final int bits) {
        if (i == nexts.length)
            throw new NoSuchElementException("Exhausted random numbers");
        return 32 == bits ? nexts[i++]
                : nexts[i++] & Integer.MAX_VALUE >> 31 - bits;
    }
}
