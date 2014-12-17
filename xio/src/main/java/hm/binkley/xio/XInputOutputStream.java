package hm.binkley.xio;

import java.io.IOException;

/**
 * {@code XInputOutputStream} is a blending of {@link XInputStream} and {@link
 * XOutputStream}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public interface XInputOutputStream
        extends XInputStream, XOutputStream {
    @Override
    default void close()
            throws IOException {
    }
}
