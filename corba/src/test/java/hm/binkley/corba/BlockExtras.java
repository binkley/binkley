package hm.binkley.corba;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * {@code BlockExtras} is a mixin interface for {@link BlockOperations}
 * providing pseudo-collection methods.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public interface BlockExtras
        extends Iterable<String>, AutoCloseable {
    /**
     * Gets the count of elements, modelled on {@link Collection#size()}.
     *
     * @return the element count
     */
    int size();

    /**
     * Checks if there are no elements, modelled on {@link
     * Collection#isEmpty()}.
     *
     * @return {@code true} if there are no elements
     */
    default boolean isEmpty() {
        return 0 == size();
    }

    /**
     * Views the "block" as a JDK 8 stream, modelled on {@link
     * Collection#stream()}.
     *
     * @return the stream, never missing
     */
    @Nonnull
    default Stream<String> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /** Releases the CORBA resources for this "block". */
    @Override
    void close();

    final class Default
            implements BlockExtras {
        private final BlockOperations corba;
        private final int nElements;

        public Default(@Nonnull final BlockOperations corba) {
            this.corba = corba;
            nElements = corba.nElements();
        }

        @Override
        public int size() {
            return nElements;
        }

        @Override
        public Iterator<String> iterator() {
            return new BlockIterator();
        }

        @Override
        public void close() {
            corba.shutdown();
        }

        private final class BlockIterator
                implements Iterator<String> {
            private int index;

            @Override
            public boolean hasNext() {
                return index < nElements;
            }

            @Override
            public String next() {
                return corba.elementAt(index++);
            }
        }
    }
}
