/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org/>.
 */

package hm.binkley.junit;

import org.junit.rules.ExternalResource;

import java.net.InetSocketAddress;
import java.net.ServerSocket;

/**
 * {@code ProvidePort} us a JUnit Rule to create an available random port number.  There is still a
 * race condition if ports on the host are allocated quickly enough to cycle through free ports
 * before the allocated port can be used in the junit test.
 * <p>
 * It opens a local server socket on port 0 and captures the allocated local port number.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public class ProvidePort
        extends ExternalResource {
    private int port;

    @Override
    protected void before()
            throws Throwable {
        try (final ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(0));
            port = serverSocket.getLocalPort();
        }
    }

    /**
     * Gets the allocated port.
     *
     * @return the allocated port
     */
    public int port() {
        return port;
    }

    @Override
    public String toString() {
        return super.toString() + "{port=" + port + '}';
    }
}
