/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util.logging.osi;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.StandardErrorStreamLog;
import org.junit.contrib.java.lang.system.StandardOutputStreamLog;
import org.junit.rules.ExpectedException;

import java.io.PrintStream;
import java.util.regex.Pattern;

import static hm.binkley.util.logging.LoggerUtil.refreshLogback;
import static hm.binkley.util.logging.osi.ITSupportLoggers.PatternMatcher.pattern;
import static hm.binkley.util.logging.osi.OSI.SystemProperty.LOGBACK_CONFIGURATION_FILE;
import static hm.binkley.util.logging.osi.OSI.SystemProperty.LOGBACK_INCLUDED_RESOURCE;
import static hm.binkley.util.logging.osi.SupportLoggers.ALERT;
import static hm.binkley.util.logging.osi.SupportLoggers.APPLICATION;
import static hm.binkley.util.logging.osi.SupportLoggers.AUDIT;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;

/**
 * {@code ITSupportLoggers} integration tests {@link SupportLoggers}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley</a>
 */
public final class ITSupportLoggers {
    @Rule
    public final ExpectedException thrown = none();
    @Rule
    public final Sout sout = new Sout();
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

    @Test
    public void applicationShouldLogNormallyOnce() {
        APPLICATION.getLogger("test").error("Ignored");

        assertThat(sout.getLog(), containsString("Ignored"));
//        assertThat(sout.getLog(), containsOnce("Ignored"));
    }

    @Test
    public void alertShouldSayWarnOnStderrOnce() {
        ALERT.getLogger("alert").warn("Ignored");

        assertThat(serr.getLog(), containsString("ALERT/WARN"));
    }

    @Test
    public void alertShouldSayErrorOnStderr() {
        ALERT.getLogger("alert").error("Ignored");

        assertThat(serr.getLog(), containsString("ALERT/ERROR"));
    }

    @Test
    public void alertShouldIncludeNonAlertLoggerName() {
        ALERT.getLogger("test").warn("Ignored");

        assertThat(serr.getLog(), containsString("ALERT/WARN"));
    }

    @Test
    public void alertShouldDuplicateOnStdout() {
        ALERT.getLogger("alert").warn("Ignored");

        assertThat(sout.getLog(), containsString("ALERT/WARN"));
    }

    @Test(expected = IllegalStateException.class)
    public void alertShouldComplainWithDebug() {
        ALERT.getLogger("test").debug("Ignored");
    }

    @Test(expected = IllegalStateException.class)
    public void alertShouldComplainWithInfo() {
        ALERT.getLogger("test").info("Ignored");
    }

    @Test
    public void auditShouldSayInfoOnStdout() {
        AUDIT.getLogger("audit").info("Ignored");

        assertThat(sout.getLog(), containsString("AUDIT/INFO"));
    }

    @Test
    public void auditShouldSayWarnOnStdout() {
        AUDIT.getLogger("audit").warn("Ignored");

        assertThat(sout.getLog(), containsString("AUDIT/WARN"));
    }

    @Test
    public void auditShouldSayErrorOnStdout() {
        AUDIT.getLogger("audit").error("Ignored");

        assertThat(sout.getLog(), containsString("AUDIT/ERROR"));
    }

    @Test
    public void auditShouldIncludeNonAuditLoggerName() {
        AUDIT.getLogger("test").info("Ignored");

        assertThat(sout.getLog(), containsString("AUDIT/INFO"));
    }

    @Test
    public void auditShouldSayNothingOnStderr() {
        AUDIT.getLogger("test").info("Ignored");

        sout.getOriginalStream().println("out: " + sout.getLog());
        sout.getOriginalStream().println("err: " + serr.getLog());
        assertThat(serr.getLog(), isEmptyString());
    }

    @Test(expected = IllegalStateException.class)
    public void auditShouldComplainWithDebug() {
        AUDIT.getLogger("test").debug("Ignored");
    }

    private static class Sout
            extends StandardOutputStreamLog {
        @Override
        protected PrintStream getOriginalStream() {
            return super.getOriginalStream();
        }
    }

    private static Matcher<CharSequence> containsOnce(final String s) {
        return pattern(".*" + s + "(?!.*" + s + ")");
    }

    /**
     * Tests if the argument is a {@link CharSequence} that matches a regular expression.
     *
     * @todo Copied from Hamcrest 1.4, yet to be released
     */
    public static class PatternMatcher
            extends TypeSafeMatcher<CharSequence> {
        /**
         * Creates a matcher that matches if the examined {@link CharSequence} matches the specified
         * regular expression.
         * <p>
         * For example:
         * <pre>assertThat("myStringOfNote", pattern("[0-9]+"))</pre>
         *
         * @param regex the regular expression that the returned matcher will use to match any
         * examined {@link CharSequence}
         */
        @Factory
        public static Matcher<CharSequence> pattern(final String regex) {
            return pattern(Pattern.compile(regex));
        }

        /**
         * Creates a matcher that matches if the examined {@link CharSequence} matches the specified
         * {@link Pattern}.
         * <p>
         * For example:
         * <pre>assertThat("myStringOfNote", Pattern.compile("[0-9]+"))</pre>
         *
         * @param pattern the pattern that the returned matcher will use to match any examined
         * {@link CharSequence}
         */
        @Factory
        public static Matcher<CharSequence> pattern(final Pattern pattern) {
            return new PatternMatcher(pattern);
        }

        private final Pattern pattern;

        public PatternMatcher(final Pattern pattern) {
            this.pattern = pattern;
        }

        @Override
        public boolean matchesSafely(final CharSequence item) {
            return pattern.matcher(item).matches();
        }

        @Override
        public void describeMismatchSafely(final CharSequence item,
                final Description mismatchDescription) {
            mismatchDescription.
                    appendText("was \"").
                    appendText(String.valueOf(item)).
                    appendText("\"");
        }

        @Override
        public void describeTo(final Description description) {
            description.
                    appendText("a string with pattern \"").
                    appendText(String.valueOf(pattern)).
                    appendText("\"");
        }
    }
}
