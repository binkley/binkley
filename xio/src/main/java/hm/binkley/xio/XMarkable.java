package hm.binkley.xio;

import java.io.IOException;

/**
 * {@code Markable} represents markable I/O streams: those which may mark a position and return to
 * it later.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public interface XMarkable {
    /**
     * Gets the most recent marked position or -1 if none.
     *
     * @return the most recent marked position or -1
     *
     * @see #mark(long)
     */
    long mark();

    /**
     * Marks the current position in this input stream. A subsequent call to the {@code reset}
     * method repositions this stream at the last marked position so that subsequent reads re-read
     * the same bytes.
     *
     * <p> The {@code mark} arguments tells this input stream to allow that many bytes to be
     * read before the mark position gets invalidated.
     *
     * <p> The general contract of {@code mark} is that, if the method
     * {@code markSupported} returns {@code true}, the stream somehow remembers all the
     * bytes read after the call to {@code mark} and stands ready to supply those same bytes
     * again if and whenever the method {@code reset} is called.  However, the stream is not
     * required to remember any data at all if more than {@code mark} bytes are read from the
     * stream before {@code reset} is called.
     *
     * <p> Marking a closed stream should not have any effect on the stream.
     *
     * <p> The {@code mark} method of {@code InputStream} does nothing.
     *
     * @param mark the maximum limit of bytes that can be read before the mark position becomes
     * invalid.
     *
     * @see #reset()
     */
    void mark(final long mark);

    /**
     * Repositions this stream to the position at the time the {@code mark} method was last
     * called on this input stream.
     *
     * <p> The general contract of {@code reset} is:
     *
     * <ul> <li> If the method {@code markSupported} returns {@code true}, then:
     *
     * <ul><li> If the method {@code mark} has not been called since the stream was created, or
     * the number of bytes read from the stream since {@code mark} was last called is larger
     * than the argument to {@code mark} at that last call, then an {@code IOException}
     * might be thrown.
     *
     * <li> If such an {@code IOException} is not thrown, then the stream is reset to a state
     * such that all the bytes read since the most recent call to {@code mark} (or since the
     * start of the file, if {@code mark} has not been called) will be resupplied to subsequent
     * callers of the {@code read} method, followed by any bytes that otherwise would have been
     * the next input data as of the time of the call to {@code reset}. </ul>
     *
     * <li> If the method {@code markSupported} returns {@code false}, then:
     *
     * <ul><li> The call to {@code reset} may throw an {@code IOException}.
     *
     * <li> If an {@code IOException} is not thrown, then the stream is reset to a fixed state
     * that depends on the particular type of the input stream and how it was created. The bytes
     * that will be supplied to subsequent callers of the {@code read} method depend on the
     * particular type of the input stream. </ul></ul>
     *
     * <p>The method {@code reset} for class {@code InputStream} does nothing except throw
     * an {@code IOException}.
     *
     * @throws IOException if this stream has not been marked or if the mark has been invalidated.
     * @see #mark(long)
     * @see IOException
     */
    void reset()
            throws IOException;
}
