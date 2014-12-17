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
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.StandardOutputStreamLog;

import static hm.binkley.util.logging.osi.OSI.SystemProperty.LOGBACK_CONFIGURATION_FILE;
import static hm.binkley.util.logging.osi.OSI.SystemProperty.LOGBACK_CONTEXT_NAME;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.contrib.java.lang.system.LogMode.LOG_ONLY;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * {@code OSIIT} tests {@link OSI}.
 *
 * @author <a href="mailto:Brian.Oxley@macquarie.com">Brian Oxley</a>
 * @todo StandardOutputStreamLog still prints to sout/serr
 * @todo StandardOutputStreamLog does not process into List of String
 */
public final class OSIApplicationNameIT {
    @Rule
    public final StandardOutputStreamLog sout = new StandardOutputStreamLog(LOG_ONLY);
    @Rule
    public final ProvideSystemProperty sysprops = new ProvideSystemProperty();

    @Before
    public void setUp() {
        sysprops.setProperty(LOGBACK_CONFIGURATION_FILE.key(), "osi-logback.xml");
        sysprops.setProperty(LOGBACK_CONTEXT_NAME.key(), null);
    }

    @Test
    public void shouldIncludeApplicationName() {
        OSI.enable("MyApp");
        getLogger("bob").info("Ignored");
        assertThat(sout.getLog(), containsString("MyApp"));
    }
}
