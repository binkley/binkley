/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.status.Status;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.contrib.java.lang.system.StandardErrorStreamLog;
import org.junit.contrib.java.lang.system.StandardOutputStreamLog;
import org.slf4j.Logger;

import java.util.List;

import static hm.binkley.util.logging.LoggerUtil.refreshLogback;
import static java.lang.System.clearProperty;
import static java.lang.System.setProperty;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.slf4j.LoggerFactory.getILoggerFactory;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * {@code MarkedConverterTest} tests {@link MatchConverter}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public final class ITMatchConverter {
    @Rule
    public StandardOutputStreamLog sout = new StandardOutputStreamLog();
    @Rule
    public StandardErrorStreamLog serr = new StandardErrorStreamLog();
    @Rule
    public RestoreSystemProperties pattern = new RestoreSystemProperties("logback.pattern");

    private String previous;

    @Before
    public void setUpITMatchConverter() {
        previous = setProperty("logback.configurationFile", "it-match-converter-logback.xml");
    }

    @After
    public void tearDownITMatchConverter() {
        if (null == previous)
            clearProperty("logback.configurationFile");
        else
            setProperty("logback.configurationFile", previous);
        clearProperty("logback.pattern");
    }

    @Test
    public void shouldMatch() {
        setProperty("logback.pattern", "%match{TRUE_COND,match,not match}");
        refreshLogback();
        getLogger("test").warn("Ignored.");
        assertLogLine(is(equalTo("match")));
    }

    @Test
    public void shouldFallbackWithoutMatch() {
        setProperty("logback.pattern", "%match{FALSE_COND,match,not match}");
        refreshLogback();
        getLogger("test").warn("Ignored.");
        assertLogLine(is(equalTo("not match")));
    }

    @Test
    public void shouldComplainWhenWrong() {
        previous = setProperty("logback.pattern", "%match");
        refreshLogback();
        final Logger log = getLogger("test");
        log.warn("Ignored.");

        final List<String> messages = ((LoggerContext) getILoggerFactory()).
                getStatusManager().
                getCopyOfStatusList().stream().
                filter(status -> Status.ERROR == status.getLevel()).
                map(Status::getMessage).
                collect(toList());
        assertThat("ERROR", messages, hasItem("Missing options for %match - missing options"));
    }

    private void assertLogLine(final Matcher<String> matcher) {
        assertThat("STDOUT", sout.getLog().trim(), matcher); // Remove trailing line ending
    }
}
