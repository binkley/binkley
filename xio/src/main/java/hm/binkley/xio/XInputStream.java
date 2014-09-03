package hm.binkley.xio;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Javadoc
 */
public interface XInputStream
        extends Closeable {
    /**
     * MAX_SKIP_BUFFER_SIZE is used to determine the maximum buffer size to use when skipping.
     *
     * @see JInputStream#MAX_SKIP_BUFFER_SIZE
     */
    int MAX_SKIP_BUFFER_SIZE = 2048;

    /**
     * Creates a new JDK {@code InputStream} forwarding all calls to this {@code XInputStream}.
     *
     * @return the new JDK input stream, never missing
     */
    @Nonnull
    default JInputStream asInputStream() {
        return new JInputStream(this);
    }

    /**
     * Reads the next byte of data from the input stream. The value byte is returned as an {@code
     * int} in the range {@code 0} to {@code 255}. If no byte is available because the end of the
     * stream has been reached, the value {@code -1} is returned. This method blocks until input
     * data is available, the end of the stream is detected, or an exception is thrown.
     *
     * @return the next byte of data, or {@code -1} if the end of the stream is reached.
     *
     * @throws IOException if an I/O error occurs.
     */
    int read()
            throws IOException;

    /**
     * Reads some number of bytes from the input stream and stores them into the buffer array {@code
     * b}. The number of bytes actually read is returned as an integer.  This method blocks until
     * input data is available, end of file is detected, or an exception is thrown.
     * <p>
     * If the length of {@code b} is zero, then no bytes are read and {@code 0} is returned;
     * otherwise, there is an attempt to read at least one byte. If no byte is available because the
     * stream is at the end of the file, the value {@code -1} is returned; otherwise, at least one
     * byte is read and stored into {@code b}.
     * <p>
     * The first byte read is stored into element {@code b[0]}, the next one into {@code b[1]}, and
     * so on. The number of bytes read is, at most, equal to the length of {@code b}. Let <i>k</i>
     * be the number of bytes actually read; these bytes will be stored in elements {@code b[0]}
     * through {@code b[}<i>k</i>{@code -1]}, leaving elements {@code b[}<i>k</i>{@code ]} through
     * {@code b[b.length-1]} unaffected.
     * <p>
     * The {@code read(b)} method for class {@code InputStream}
     * has the same effect as: <pre>{@code read(b, 0, b.length) }</pre>
     *
     * @param b the buffer into which the data is read.
     *
     * @return the total number of bytes read into the buffer, or {@code -1} if there is no more
     * data because the end of the stream has been reached.
     *
     * @throws IOException If the first byte cannot be read for any reason other than the end of the
     * file, if the input stream has been closed, or if some other I/O error occurs.
     * @throws NullPointerException if {@code b} is {@code null}.
     * @see XInputStream#read(byte[], int, int)
     */
    default int read(@Nonnull final byte[] b)
            throws IOException {
        return read(b, 0, b.length);
    }

    /**
     * Reads up to {@code len} bytes of data from the input stream into an array of bytes.  An
     * attempt is made to read as many as {@code len} bytes, but a smaller number may be read. The
     * number of bytes actually read is returned as an integer.
     * <p>
     * This method blocks until input data is available, end of file is detected, or an exception is
     * thrown.
     * <p>
     * If {@code len} is zero, then no bytes are read and {@code 0} is returned; otherwise, there is
     * an attempt to read at least one byte. If no byte is available because the stream is at end of
     * file, the value {@code -1} is returned; otherwise, at least one byte is read and stored into
     * {@code b}.
     * <p>
     * The first byte read is stored into element {@code b[off]}, the next one into {@code
     * b[off+1]}, and so on. The number of bytes read is, at most, equal to {@code len}. Let
     * <i>k</i> be the number of bytes actually read; these bytes will be stored in elements {@code
     * b[off]} through {@code b[off+}<i>k</i>{@code -1]}, leaving elements {@code
     * b[off+}<i>k</i>{@code ]} through {@code b[off+len-1]} unaffected.
     * <p>
     * In every case, elements {@code b[0]} through {@code b[off]} and elements {@code b[off+len]}
     * through {@code b[b.length-1]} are unaffected.
     * <p>
     * The {@code read(b, off, len)} method for class {@code InputStream} simply calls the method
     * {@code read()} repeatedly. If the first such call results in an {@code IOException}, that
     * exception is returned from the call to the {@code read(b, off, len)} method.  If any
     * subsequent call to {@code read()} results in a {@code IOException}, the exception is caught
     * and treated as if it were end of file; the bytes read up to that point are stored into {@code
     * b} and the number of bytes read before the exception occurred is returned. The default
     * implementation of this method blocks until the requested amount of input data {@code len} has
     * been read, end of file is detected, or an exception is thrown. Subclasses are encouraged to
     * provide a more efficient implementation of this method.
     *
     * @param b the buffer into which the data is read.
     * @param off the start offset in array {@code b} at which the data is written.
     * @param len the maximum number of bytes to read.
     *
     * @return the total number of bytes read into the buffer, or {@code -1} if there is no more
     * data because the end of the stream has been reached.
     *
     * @throws IOException If the first byte cannot be read for any reason other than end of file,
     * or if the input stream has been closed, or if some other I/O error occurs.
     * @throws NullPointerException If {@code b} is {@code null}.
     * @throws IndexOutOfBoundsException If {@code off} is negative, {@code len} is negative, or
     * {@code len} is greater than {@code b.length - off}
     * @see XInputStream#read()
     */
    default int read(@Nonnull final byte[] b, final int off, final int len)
            throws IOException {
        if (0 == len)
            return 0;
        if (0 > off || 0 > len || len > b.length - off)
            throw new IndexOutOfBoundsException();

        int c = read();
        if (-1 == c)
            return -1;
        b[off] = (byte) c;

        int i = 1;
        try {
            for (; i < len; i++) {
                c = read();
                if (-1 == c)
                    break;
                b[off + i] = (byte) c;
            }
        } catch (final IOException ignored) {
        }
        return i;
    }

    /**
     * Skips over and discards {@code n} bytes of data from this input stream. The {@code skip}
     * method may, for a variety of reasons, end up skipping over some smaller number of bytes,
     * possibly {@code 0}. This may result from any of a number of conditions; reaching end of file
     * before {@code n} bytes have been skipped is only one possibility. The actual number of bytes
     * skipped is returned. If {@code n} is negative, the {@code skip} method for class {@code
     * InputStream} always returns 0, and no bytes are skipped. Subclasses may handle the negative
     * value differently.
     * <p>
     * The {@code skip} method of this class creates a byte array and then repeatedly reads into it
     * until {@code n} bytes have been read or the end of the stream has been reached. Subclasses
     * are encouraged to provide a more efficient implementation of this method. For instance, the
     * implementation may depend on the ability to seek.
     *
     * @param n the number of bytes to be skipped.
     *
     * @return the actual number of bytes skipped.
     *
     * @throws IOException if the stream does not support seek, or if some other I/O error occurs.
     */
    default long skip(final long n)
            throws IOException {
        long remaining = n;

        if (0 >= n)
            return 0;

        final int size = (int) Math.min(MAX_SKIP_BUFFER_SIZE, remaining);
        final byte[] skipBuffer = new byte[size];
        while (0 < remaining) {
            final int nr = read(skipBuffer, 0, (int) Math.min(size, remaining));
            if (0 > nr)
                break;
            remaining -= nr;
        }

        return n - remaining;
    }

    /**
     * Returns an estimate of the number of bytes that can be read (or skipped over) from this input
     * stream without blocking by the next invocation of a method for this input stream. The next
     * invocation might be the same thread or another thread.  A single read or skip of this many
     * bytes will not block, but may read or skip fewer bytes.
     * <p>
     * Note that while some implementations of {@code InputStream} will return the total number of
     * bytes in the stream, many will not.  It is never correct to use the return value of this
     * method to allocate a buffer intended to hold all data in this stream.
     * <p>
     * A subclass' implementation of this method may choose to throw an {@link IOException} if this
     * input stream has been closed by invoking the {@link #close()} method.
     * <p>
     * The {@code available} method for class {@code InputStream} always returns {@code 0}.
     * <p>
     * This method should be overridden by subclasses.
     *
     * @return an estimate of the number of bytes that can be read (or skipped over) from this input
     * stream without blocking or {@code 0} when it reaches the end of the input stream.
     *
     * @throws IOException if an I/O error occurs.
     */
    default int available()
            throws IOException {
        return 0;
    }

    /**
     * Closes this input stream and releases any system resources associated with the stream.
     * <p>
     * The {@code close} method of {@code InputStream} does nothing.
     *
     * @throws IOException if an I/O error occurs.
     */
    default void close()
            throws IOException {
    }

    class JInputStream
            extends InputStream {
        private final XInputStream in;

        protected JInputStream(@Nonnull final XInputStream in) {
            this.in = in;
        }

        @Override
        public int read()
                throws IOException {
            return in.read();
        }

        @Override
        public int read(@Nonnull final byte[] b)
                throws IOException {
            return in.read(b);
        }

        @Override
        public int read(@Nonnull final byte[] b, final int off, final int len)
                throws IOException {
            return in.read(b, off, len);
        }

        @Override
        public long skip(final long n)
                throws IOException {
            return in.skip(n);
        }

        @Override
        public int available()
                throws IOException {
            return in.available();
        }

        @Override
        public void close()
                throws IOException {
            in.close();
        }

        @Override
        public synchronized void mark(final int readlimit) {
            if (markSupported())
                ((XMarkable) in).mark(readlimit);
        }

        @Override
        public synchronized void reset()
                throws IOException {
            if (markSupported())
                ((XMarkable) in).reset();
            else
                throw new IOException("mark/reset not supported");
        }

        @Override
        public boolean markSupported() {
            return in instanceof XMarkable;
        }
    }
}
