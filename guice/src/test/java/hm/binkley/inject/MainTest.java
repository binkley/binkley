/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.inject;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@code MainTest} tests {@link Main}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public class MainTest {
    private static Map<String, String> none() {
        return emptyMap();
    }

    private static Map<String, String> only(final String flag, final Object value) {
        return singletonMap(flag, String.valueOf(value));
    }

    @Test
    public void withNothing() {
        final OptionParser parser = new OptionParser();
        final OptionSet options = parser.parse();
        assertThat(Main.mapOf(options), is(equalTo(none())));
    }

    @Test
    public void withoutValue() {
        final OptionParser parser = new OptionParser();
        parser.accepts("debug");
        final OptionSet options = parser.parse("--debug");
        assertThat(Main.mapOf(options), is(equalTo(only("debug", true))));
    }

    @Ignore("Not yet supported in jopt-simple")
    @Test
    public void withNegation() {
        final OptionParser parser = new OptionParser();
        parser.accepts("debug");
        final OptionSet options = parser.parse("+debug");
        assertThat(Main.mapOf(options), is(equalTo(only("debug", false))));
    }

    @Test
    public void withValue() {
        final OptionParser parser = new OptionParser();
        parser.accepts("debug").withRequiredArg();
        final OptionSet options = parser.parse("--debug=3");
        assertThat(Main.mapOf(options), is(equalTo(only("debug", 3))));
    }

    @Test
    public void withAlternates() {
        final OptionParser parser = new OptionParser();
        parser.acceptsAll(asList("x", "debug"));
        final OptionSet options = parser.parse("-x");
        assertThat(Main.mapOf(options), is(equalTo(only("debug", true))));
    }

    @Test
    public void withMultiValues() {
        final OptionParser parser = new OptionParser();
        parser.accepts("debug").withRequiredArg().withValuesSeparatedBy(',');
        final OptionSet options = parser.parse("--debug=yes,no,yes");
        assertThat(Main.mapOf(options), is(equalTo(only("debug", asList("yes", "no", "yes")))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void withoutGoodFlag() {
        final OptionParser parser = new OptionParser();
        parser.accepts("x");
        final OptionSet options = parser.parse("-x");
        Main.mapOf(options);
    }
}
