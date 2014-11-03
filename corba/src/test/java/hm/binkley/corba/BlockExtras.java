package hm.binkley.corba;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * {@code BlockExtras} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public interface BlockExtras
        extends Iterable<String>, AutoCloseable {
    int size();

    default boolean isEmpty() {
        return 0 == size();
    }

    default Stream<String> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    // Override to remove exception declaration
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
