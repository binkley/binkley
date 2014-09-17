/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util.logging.osi;

import hm.binkley.util.logging.osi.OSI.SystemProperty;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.StandardErrorStreamLog;
import org.junit.contrib.java.lang.system.StandardOutputStreamLog;

import static hm.binkley.util.logging.LoggerUtil.refreshLogback;
import static hm.binkley.util.logging.osi.OSI.SystemProperty.LOGBACK_CONFIGURATION_FILE;
import static hm.binkley.util.logging.osi.OSI.SystemProperty.LOGBACK_INCLUDED_RESOURCE;
import static hm.binkley.util.logging.osi.SupportLoggers.ALERT;
import static hm.binkley.util.logging.osi.SupportLoggers.APPLICATION;
import static hm.binkley.util.logging.osi.SupportLoggers.AUDIT;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;

/**
 * {@code ITSupportLoggers} integration tests {@link SupportLoggers}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley</a>
 */
public final class ITSupportLoggers {
    @Rule
    public final StandardOutputStreamLog sout = new StandardOutputStreamLog();
    @Rule
    public final StandardErrorStreamLog serr = new StandardErrorStreamLog();
    @Rule
    public final ProvideSystemProperty osi = new ProvideSystemProperty(
            LOGBACK_CONFIGURATION_FILE.key(), "osi-logback.xml");
    @Rule
    public final ProvideSystemProperty included = new ProvideSystemProperty(
            LOGBACK_INCLUDED_RESOURCE.key(), "osi-support-loggers-included.xml");

    @Before
    public void setUp() {
        refreshLogback();
    }

    @Test(expected = IllegalStateException.class)
    public void alertShouldRejectTrivialMessages() {
        ALERT.getLogger("test").info("Ignored");
    }

    @Test
    public void applicationShouldLogNormally() {
        APPLICATION.getLogger("test").error("Test");

        assertThat(sout.getLog(), containsString("Test"));
    }

    @Test
    public void alertShouldSayAlertWarnOnStderr() {
        ALERT.getLogger("test").warn("Ignored");

        assertThat(serr.getLog(), containsString("ALERT/WARN"));
    }

    @Test
    public void alertShouldSayNothingOnStdout() {
        ALERT.getLogger("test").warn("Ignored");

        assertThat(sout.getLog(), isEmptyString());
    }

    @Test(expected = IllegalStateException.class)
    public void alertComplainWithInfo() {
        ALERT.getLogger("test").info("Ignored");
    }

    @Test
    public void auditShouldSayAuditInfoOnStdout() {
        AUDIT.getLogger("test").info("Ignored");

        assertThat(serr.getLog(), containsString("AUDIT/INFO"));
        assertThat(sout.getLog(), containsString("AUDIT/INFO"));
    }

    @Test
    public void auditShouldSayNothingOnStderr() {
        AUDIT.getLogger("test").info("Ignored");

        assertThat(serr.getLog(), isEmptyString());
    }

    @Test(expected = IllegalStateException.class)
    public void auditShouldComplainWithDebug() {
        AUDIT.getLogger("test").debug("Ignored");
    }
}
