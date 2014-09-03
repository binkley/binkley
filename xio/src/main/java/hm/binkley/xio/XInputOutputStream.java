package hm.binkley.xio;

import java.io.IOException;

/**
 * {@code XInputOutputStream} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public interface XInputOutputStream
        extends XInputStream, XOutputStream {
    @Override
    default void close()
            throws IOException {
    }
}
