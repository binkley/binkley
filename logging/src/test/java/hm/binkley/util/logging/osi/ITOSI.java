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

package hm.binkley.util.logging.osi;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.StandardOutputStreamLog;
import org.slf4j.LoggerFactory;

import static hm.binkley.util.logging.LoggerUtil.refreshLogback;
import static hm.binkley.util.logging.osi.OSI.SystemProperty.LOGBACK_CONFIGURATION_FILE;
import static hm.binkley.util.logging.osi.OSI.SystemProperty.LOGBACK_JANSI;
import static hm.binkley.util.logging.osi.OSI.SystemProperty.LOGBACK_STYLES_RESOURCE;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@code OSIIT} tests {@link OSI}.
 *
 * @author <a href="mailto:Brian.Oxley@macquarie.com">Brian Oxley</a>
 * @todo StandardOutputStreamLog still prints to sout/serr
 * @todo StandardOutputStreamLog does not process into List of String
 */
public final class ITOSI {
    @Rule
    public final StandardOutputStreamLog sout = new StandardOutputStreamLog();
    @Rule
    public final ProvideSystemProperty props = new ProvideSystemProperty(
            LOGBACK_CONFIGURATION_FILE.key(), "osi-logback.xml").
            and(LOGBACK_JANSI.key(), null);

    @Before
    public void setUp() {
        refreshLogback();
    }

    @Test
    public void shouldIncludeApplicationName() {
        OSI.enable("MyApp");
        LoggerFactory.getLogger("bob").error("Ignored");
        assertThat(sout.getLog(), containsString("MyApp"));
    }

    @Ignore("How to test? Jansi cannot setup up terminal on wrapped stream")
    @Test
    public void shouldIncludeAnsiEscapes() {
        LOGBACK_JANSI.set("true");
        LOGBACK_STYLES_RESOURCE.set("osi-logback-jansi-styles.properties");
        refreshLogback();
        LoggerFactory.getLogger("bob").error("Ignored");
        assertThat(sout.getLog(), is(equalTo("Ignored")));
    }
}
