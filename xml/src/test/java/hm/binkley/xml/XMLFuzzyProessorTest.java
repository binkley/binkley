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

package hm.binkley.xml;

import org.intellij.lang.annotations.Language;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * {@code FuzzyTest} tests {@link XMLFuzzyProcessor}
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public class XMLFuzzyProessorTest {
    @Language("XML")
    public static final String WAS_HE
            = "<wasHe><needsNoConversion>abc</needsNoConversion><isAPrimitive>3</isAPrimitive><usesParse>1970-01-01T00:00:00Z</usesParse><usesConstructor>1.234</usesConstructor><throwsAnException>not:a-uri</throwsAnException></wasHe>";
    private WasHe wasHe;

    @Before
    public void setUp()
            throws Exception {
        final InputSource source = new InputSource(new StringReader(WAS_HE));
        source.setEncoding("UTF-8");
        wasHe = WasHeFactory
                .of(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(source));
    }

    @Test
    public void shouldHandleNeedsNoConversion() {
        assertThat(wasHe.needsNoConversion(), is(equalTo("abc")));
    }

    @Test
    public void shouldHandleNullOk() {
        assertThat(wasHe.nullOk(), is(nullValue()));
    }
}
