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

package hm.binkley.annotation.processing;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@code NamesTest} tests {@link Names}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
@RunWith(Parameterized.class)
public class NamesTest {
    @Parameter(0)
    public String packajIn;
    @Parameter(1)
    public String nameIn;
    @Parameter(2)
    public String parentIn;
    @Parameter(3)
    public String fullName;
    @Parameter(4)
    public String packaj;
    @Parameter(5)
    public String name;
    @Parameter(6)
    public String relativeTo;

    @Parameters(name = "{index}: ({0}, {1}, {2}) -> ({3}, {4}, {5}): {6}")
    public static Collection parameters() {
        return asList(args("", "Foo", "Bar", "Foo", "", "Foo", "Bar"),
                args("", "flarb.Foo", "flarb.Bar", "flarb.Foo", "flarb",
                        "Foo", "Bar"),
                args("flarb", "Foo", "Bar", "flarb.Foo", "flarb", "Foo",
                        "Bar"),
                args("flarb", "mumble.Foo", "mumble.Bar", "flarb.mumble.Foo",
                        "flarb.mumble", "Foo", "Bar"),
                args("flarb", "mumble.Foo", "mumble.Bar", "flarb.mumble.Foo",
                        "flarb.mumble", "Foo", "Bar"));
    }

    @Test
    public void shouldWork() {
        final Names zis = Names.from(packajIn, nameIn);
        final Names zuper = Names.from(packajIn, parentIn);
        assertThat("fullname", zis.fullName, is(equalTo(fullName)));
        assertThat("packaj", zis.packaj, is(equalTo(packaj)));
        assertThat("name", zis.name, is(equalTo(name)));
        assertThat("relativeTo", zuper.nameRelativeTo(zis),
                is(equalTo(relativeTo)));
    }

    private static Object[] args(final String packajIn, final String nameIn,
            final String parentIn, final String fullName, final String packaj,
            final String name, final String relativeTo) {
        return new Object[]{packajIn, nameIn, parentIn, fullName, packaj,
                name, relativeTo};
    }
}
