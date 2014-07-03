/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util.logging;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static hm.binkley.util.logging.LoggerUtil.refreshLogback;
import static java.lang.System.clearProperty;
import static java.lang.System.setProperty;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * {@code MarkedConverterTest} tests {@link MatchConverter}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public final class ITMatchConverter
        extends AbstractITLogback {
    private String previous;

    @Before
    public void setUpITMatchConverter() {
        previous = setProperty("logback.configurationResource", "it-match-converter-logback.xml");
    }

    @After
    public void tearDownITMatchConverter() {
        if (null == previous)
            clearProperty("logback.configurationResource");
        else
            setProperty("logback.configurationResource", previous);
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
        getLogger("test").warn("Ignored.");
        assertLogLine(containsString("Missing options for %match - null"));
    }
}
