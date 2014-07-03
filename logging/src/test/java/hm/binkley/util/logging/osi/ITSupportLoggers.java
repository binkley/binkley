/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util.logging.osi;

import org.junit.Before;
import org.junit.Test;

import static hm.binkley.util.logging.osi.OSI.SystemProperty.LOGBACK_INCLUDED_RESOURCE;
import static hm.binkley.util.logging.osi.SupportLoggers.ALERT;
import static hm.binkley.util.logging.osi.SupportLoggers.APPLICATION;
import static hm.binkley.util.logging.osi.SupportLoggers.AUDIT;
import static org.hamcrest.Matchers.containsString;

/**
 * {@code ITSupportLoggers} integration tests {@link SupportLoggers}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley</a>
 */
public final class ITSupportLoggers
        extends AbstractITOSI {
    @Before
    public void setUpITSupportLogger() {
        setOSISystemProperty(LOGBACK_INCLUDED_RESOURCE, "osi-support-loggers-included.xml");
    }

    @Test(expected = IllegalStateException.class)
    public void alertShouldRejectTrivialMessages() {
        ALERT.getLogger("test").info("Ignored");
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void applicationShouldLogNormally() {
        APPLICATION.getLogger("test").error("Test");
        assertLogLine(containsString("Test"));
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void alertShouldSayAlertWarn() {
        ALERT.getLogger("test").warn("Ignored");
        assertLogLine(containsString("ALERT/WARN"));
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void auditShouldSayAuditWarn() {
        AUDIT.getLogger("test").warn("Ignored");
        assertLogLine(containsString("AUDIT/WARN"));
    }
}
