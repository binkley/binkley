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

package hm.binkley.lombok;

import org.junit.Ignore;
import org.junit.Test;

import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@code ThreadNamedTest} tests {@link ThreadNamed}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public final class ThreadNamedTest {
    private static final String newThreadName = "Bob the Builder";

    @ThreadNamed("Bob the Builder")
    @Test
    public void shouldRenameThreadFromLiteral() {
        assertThat(currentThread().getName(), is(equalTo("Bob the Builder")));
    }

    @Ignore("TODO: This may be unsupported in lombok - investigate")
    @ThreadNamed(newThreadName)
    @Test
    public void shouldRenameThreadFromGlobalConstant() {
        assertThat(currentThread().getName(), is(equalTo(newThreadName)));
    }

    @Test
    public void shouldRenameThreadWithStringFormatting() {
        assertThat(doLittle(3, "4", true), is(equalTo(format("%1$d %3$b", 3, "4", true))));
    }

    @ThreadNamed("Bob the Builder")
    @Test
    public void shouldIgnoreMethodParamsIfNoFormatting() {
        assertThat(doLess(7), is(equalTo("Bob the Builder")));
    }

    @ThreadNamed("%1$d %3$b")
    private static String doLittle(final int a, final String b, final boolean c) {
        return currentThread().getName();
    }

    private static String doLess(final int a) {
        return currentThread().getName();
    }
}
