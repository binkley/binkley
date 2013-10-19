/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.inject;

import joptsimple.OptionSet;
import org.junit.Test;

import static com.google.inject.Guice.createInjector;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@code JOptSimpleModuleTest} tests {@link JOptSimpleModule}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public class JOptSimpleModuleTest {
    @Test
    public void shouldParse() {
        assertThat(createInjector(JOptSimpleModule.jOptSimpleModule()).getInstance(OptionSet.class)
                .nonOptionArguments().isEmpty(), is(true));
    }
}
