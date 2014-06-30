package hm.binkley.xio;

import hm.binkley.util.Bug;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * {@code XByteArrayInputOutputStream} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public class XByteBufferInputOutputStream
        implements XInputOutputStream, XSeekable {
    private final ByteBuffer buf;
    private int mark;

    public XByteBufferInputOutputStream(final ByteBuffer buf) {
        this.buf = buf;
    }

    @Override
    public int read() {
        return buf.get();
    }

    @Override
    public void write(final int b)
            throws IOException {
        buf.put((byte) b);
    }

    @Override
    public void write(@Nonnull final byte[] b, final int off, final int len)
            throws IOException {
        buf.put(b, off, len);
    }

    @Override
    public long mark() {
        return mark;
    }

    @Override
    public void mark(final long mark) {
        this.mark = (int) mark;
    }

    @Override
    public long seek(final long offset, @Nonnull final Whence whence)
            throws IOException {
        try {
            switch (whence) {
            case SET:
                buf.position((int) offset);
                break;
            case CUR:
                buf.position((int) offset + buf.position());
                break;
            case END:
                buf.position((int) offset + buf.limit());
                break;
            default:
                throw new Bug("Missing case: " + whence);
            }
            return buf.position();
        } catch (final IllegalArgumentException e) {
            throw new IOException(e);
        }
    }
}
