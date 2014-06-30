package hm.binkley.xio;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Javadoc
 */
public interface XOutputStream
        extends Closeable, Flushable {
    /**
     * Creates a JDK {@code OutputStream} implementation forwarding all calls to this {@code
     * XOutputStream}.
     *
     * @return the new JDK output stream, never missing
     */
    @Nonnull
    default JOutputStream asOutputStream() {
        return new JOutputStream(this);
    }

    /**
     * Writes the specified byte to this output stream. The general contract for {@code write} is
     * that one byte is written to the output stream. The byte to be written is the eight low-order
     * bits of the argument {@code b}. The 24 high-order bits of {@code b} are ignored. <p>
     * Subclasses of {@code OutputStream} must provide an implementation for this method.
     *
     * @param b the {@code byte}.
     *
     * @throws IOException if an I/O error occurs. In particular, an {@code IOException} may be
     * thrown if the output stream has been closed.
     */
    void write(final int b)
            throws IOException;

    /**
     * Writes {@code b.length} bytes from the specified byte array to this output stream. The
     * general contract for {@code write(b)} is that it should have exactly the same effect as the
     * call {@code write(b, 0, b.length)}.
     *
     * @param b the data.
     *
     * @throws IOException if an I/O error occurs.
     * @see XOutputStream#write(byte[], int, int)
     */
    default void write(@Nonnull final byte[] b)
            throws IOException {
        write(b, 0, b.length);
    }

    /**
     * Writes {@code len} bytes from the specified byte array starting at offset {@code off} to this
     * output stream. The general contract for {@code write(b, off, len)} is that some of the bytes
     * in the array {@code b} are written to the output stream in order; element {@code b[off]} is
     * the first byte written and {@code b[off+len-1]} is the last byte written by this operation.
     * <p> The {@code write} method of {@code OutputStream} calls the write method of one argument
     * on each of the bytes to be written out. Subclasses are encouraged to override this method and
     * provide a more efficient implementation. <p> If {@code b} is {@code null}, a {@code
     * NullPointerException} is thrown. <p> If {@code off} is negative, or {@code len} is negative,
     * or {@code off+len} is greater than the length of the array {@code b}, then an
     * <tt>IndexOutOfBoundsException</tt> is thrown.
     *
     * @param b the data.
     * @param off the start offset in the data.
     * @param len the number of bytes to write.
     *
     * @throws IOException if an I/O error occurs. In particular, an {@code IOException} is thrown
     * if the output stream is closed.
     */
    default void write(@Nonnull final byte[] b, final int off, final int len)
            throws IOException {
        if (len == 0)
            return;
        if (off < 0 || off > b.length || len < 0 || off + len > b.length || off + len < 0)
            throw new IndexOutOfBoundsException();
        for (int i = 0; i < len; i++)
            write(b[off + i]);
    }

    /**
     * Flushes this output stream and forces any buffered output bytes to be written out. The
     * general contract of {@code flush} is that calling it is an indication that, if any bytes
     * previously written have been buffered by the implementation of the output stream, such bytes
     * should immediately be written to their intended destination. <p> If the intended destination
     * of this stream is an abstraction provided by the underlying operating system, for example a
     * file, then flushing the stream guarantees only that bytes previously written to the stream
     * are passed to the operating system for writing; it does not guarantee that they are actually
     * written to a physical device such as a disk drive. <p> The {@code flush} method of {@code
     * OutputStream} does nothing.
     *
     * @throws IOException if an I/O error occurs.
     */
    default void flush()
            throws IOException {
    }

    /**
     * Closes this output stream and releases any system resources associated with this stream. The
     * general contract of {@code close} is that it closes the output stream. A closed stream cannot
     * perform output operations and cannot be reopened. <p> The {@code close} method of {@code
     * OutputStream} does nothing.
     *
     * @throws IOException if an I/O error occurs.
     */
    default void close()
            throws IOException {
    }

    public class JOutputStream
            extends OutputStream {
        private final XOutputStream out;

        protected JOutputStream(final XOutputStream out) {
            this.out = out;
        }

        @Override
        public void write(final int b)
                throws IOException {
            out.write(b);
        }

        @Override
        public void write(@Nonnull final byte[] b)
                throws IOException {
            out.write(b);
        }

        @Override
        public void write(@Nonnull final byte[] b, final int off, final int len)
                throws IOException {
            out.write(b, off, len);
        }

        @Override
        public void flush()
                throws IOException {
            out.flush();
        }

        @Override
        public void close()
                throws IOException {
            out.close();
        }
    }
}
