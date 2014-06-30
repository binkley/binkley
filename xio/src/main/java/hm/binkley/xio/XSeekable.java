package hm.binkley.xio;

import javax.annotation.Nonnull;
import java.io.IOException;

import static hm.binkley.xio.XSeekable.Whence.CUR;
import static hm.binkley.xio.XSeekable.Whence.END;
import static hm.binkley.xio.XSeekable.Whence.SET;

/**
 * {@code XSeekable} represents seekable I/O streams: those which may be programmatically moved to
 * another read/write position.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public interface XSeekable
        extends XMarkable {
    /** Marker of relative nature of offset for {@link #seek(long, Whence)}. */
    enum Whence {
        /** Offset are relative to the beginning of the stream.  Offset 0 is the first stream byte. */
        SET,
        /** Offsets are relative to the current stream position.  Offset 0 is the current position. */
        CUR,
        /** Offsets are relative to the end of the stream.  Offset 0 is the last stream byte. */
        END
    }

    /**
     * Moves the current stream position to the given <var>offset</var> relative to
     * <var>whence</var>.
     * <p>
     * No combination of <var>offset</var> and <var>whence</var> may refer beyond the stream limits.
     * If <var>whence</var> is {@link Whence#SET} <var>offset</var> must be positive. If
     * <var>whence</var> is {@link Whence#END} <var>offset</var> must be negative.  If
     * <var>whence</var> is {@link Whence#CUR} offset may be positive or negative.
     *
     * @param offset the offset
     * @param whence the relative position, never missing
     *
     * @return the new file position
     *
     * @throws IOException if seek fails or <var>offset</var> is out of range for the stream
     * relative to <var>whence</var>
     */
    long seek(final long offset, @Nonnull final Whence whence)
            throws IOException;

    /** @todo What is going on here?  Won't this leave the stream at the end? */
    default long size()
            throws IOException {
        return seek(0, END);
    }

    /**
     * Gets the current stream position without changing it.
     *
     * @return the current stream position, always non-negatives
     *
     * @throws IOException if seek fails
     */
    default long tell()
            throws IOException {
        return seek(0, CUR);
    }

    /**
     * Restores the stream to its initial postion.
     *
     * @throws IOException if seek fails
     */
    default void rewind()
            throws IOException {
        seek(0, SET);
    }

    /**
     * Resets the position to the most recent mark using {@link #seek(long, Whence) seek(mark(),
     * SET}.
     */
    @Override
    default void reset()
            throws IOException {
        seek(mark(), SET);
    }
}
