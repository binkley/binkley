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

package hm.binkley.util;

import hm.binkley.util.StringX.FormatResult;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@code StringXTest} tests {@link StringX}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public final class StringXTest {
    @Test
    public void shouldFormatWithNewSpecifiers() {
        final StringX stringx = new StringX(2);
        stringx.put('!', "Dog barking");
        stringx.put('^', "Cat mewling");

        assertThat(stringx.format("Foo saw %! and heard %^."),
                is(equalTo("Foo saw Dog barking and heard Cat mewling.")));
    }

    @Test
    public void shouldFormatWithPercentSign() {
        final StringX stringx = new StringX(1);
        stringx.put('!', "%Foo%");

        assertThat(stringx.format("This is %!."),
                is(equalTo("This is %Foo%.")));
    }

    @Test
    public void shouldFormatWithNewAndOldSpecifiers() {
        final StringX stringx = new StringX(1);
        stringx.put('!',
                sargs -> new FormatResult("Hi, " + sargs.args[sargs.n], 1,
                        false));

        assertThat(
                stringx.format("Foo said '%!' and heard %s.", "Bob", "Sally"),
                is(equalTo("Foo said 'Hi, Bob' and heard Sally.")));
    }
}
