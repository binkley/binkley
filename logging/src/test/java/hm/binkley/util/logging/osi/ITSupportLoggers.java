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

import java.util.regex.Pattern;

import static hm.binkley.util.logging.LoggerUtil.refreshLogback;
import static hm.binkley.util.logging.osi.ITSupportLoggers.PatternMatcher.pattern;
import static hm.binkley.util.logging.osi.OSI.SystemProperty.LOGBACK_CONFIGURATION_FILE;
import static hm.binkley.util.logging.osi.OSI.SystemProperty.LOGBACK_INCLUDED_RESOURCE;
import static hm.binkley.util.logging.osi.SupportLoggers.ALERT;
import static hm.binkley.util.logging.osi.SupportLoggers.APPLICATION;
import static hm.binkley.util.logging.osi.SupportLoggers.AUDIT;
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

    @Test
    public void applicationShouldLogNormallyOnce() {
        APPLICATION.getLogger("test").error("Ignored");

        assertThat(sout.getLog(), containsOnce("Ignored"));
    }

    @Test
    public void alertShouldSayWarnOnStderrOnce() {
        ALERT.getLogger("alert").warn("Ignored");

        assertThat(serr.getLog(), containsOnce("ALERT/WARN"));
    }

    @Test
    public void alertShouldSayErrorOnStderrOnce() {
        ALERT.getLogger("alert").error("Ignored");

        assertThat(serr.getLog(), containsOnce("ALERT/ERROR"));
    }

    @Test
    public void alertShouldIncludeNonAlertLoggerName() {
        ALERT.getLogger("test").warn("Ignored");

        assertThat(serr.getLog(), containsOnce("ALERT/WARN"));
    }

    @Test
    public void alertShouldDuplicateOnStdoutOnce() {
        ALERT.getLogger("alert").warn("Ignored");

        assertThat(sout.getLog(), containsOnce("ALERT/WARN"));
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
    public void auditShouldSayInfoOnStdoutTwice() {
        AUDIT.getLogger("audit").info("Ignored");

        // TODO: How to get it only once when AUDIT goes to stdout?
        assertThat(sout.getLog(), containsTwice("AUDIT/INFO"));
    }

    @Test
    public void auditShouldSayWarnOnStdoutTwice() {
        AUDIT.getLogger("audit").warn("Ignored");

        assertThat(sout.getLog(), containsTwice("AUDIT/WARN"));
    }

    @Test
    public void auditShouldSayErrorOnStdoutTwice() {
        AUDIT.getLogger("audit").error("Ignored");

        assertThat(sout.getLog(), containsTwice("AUDIT/ERROR"));
    }

    @Test
    public void auditShouldIncludeNonAuditLoggerNameTwice() {
        AUDIT.getLogger("test").info("Ignored");

        assertThat(sout.getLog(), containsTwice("AUDIT/INFO"));
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

    private static Matcher<CharSequence> containsOnce(final String s) {
        // TODO: Now you have two problems
        return pattern("(?s)([^\n]*" + s + "[^\n]*\n)(?!\\1)");
    }

    private static Matcher<CharSequence> containsTwice(final String s) {
        // TODO: Now you have two problems
        return pattern("(?s)([^\n]*" + s + "[^\n]*\n)\\1");
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
