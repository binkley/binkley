/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util.logging;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.getProperties;
import static java.lang.System.setOut;
import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertThat;
import static org.slf4j.MDC.getCopyOfContextMap;
import static org.slf4j.MDC.setContextMap;

/**
 * Base class for logback integration tests.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public abstract class AbstractITLogback {
    private PrintStream stdout;
    private OutputStream out;
    private Map<Object, Object> systemProperties;
    private Map contextMap;

    /**
     * Asserts the log line matches <var>matcher</var>.
     *
     * @param matcher the matcher, never missing
     */
    protected final void assertLogLine(@Nonnull final Matcher<String> matcher) {
        assertThat(logLine(), matcher);
    }

    /**
     * Gets the log line for more specialized tests.
     *
     * @return the log line, never missing
     */
    @Nonnull
    protected final String logLine() {
        return out.toString();
    }

    @Before
    public void setUpAbstractITLogback() {
        stdout = System.out;
        out = new ByteArrayOutputStream();
        setOut(new PrintStream(out));
        systemProperties = new HashMap<>(getProperties()); // Make a copy
        contextMap = getCopyOfContextMap();
        setContextMap(null == contextMap ? emptyMap() : new HashMap(contextMap));
    }

    @After
    public void tearDownAbstractITLogback() {
        setOut(stdout);
        getProperties().clear();
        getProperties().putAll(systemProperties);
        setContextMap(null == contextMap ? emptyMap() : contextMap);
    }
}
