package hm.binkley.configuration;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static java.lang.String.format;
import static jnr.posix.POSIXFactory.getPOSIX;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * {@code SpringFormatConfigurationTest} tests {@link SpringFormatConfiguration}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public final class SpringFormatConfigurationTest {
    private static final String KEY = "eg.bar";
    @Rule
    public ExpectedException exception = ExpectedException.none();
    private EgSpringConfiguration configuration;

    private static void setenv(final String name, final String value) {
        final int setenv = getPOSIX().setenv(name, value, 1);
        if (0 > setenv)
            throw new RuntimeException(format("Cannot setenv (%d): %s: %s", setenv, name, value));
    }

    @Before
    public void setUp()
            throws Exception {
        configuration = new EgSpringConfiguration();
    }

    @Test
    public void shouldReturnRightValueWhenEarlierPropertiesOverridesLater() {
        assertThat(configuration.getBar(), is(equalTo(4)));
    }

    @Test
    public void shouldReturnRightValueWhenSystemPropertiesOverridesProperties() {
        System.setProperty(KEY, "7");
        assertThat(configuration.getBar(), is(equalTo(7)));
    }

    @Test
    public void shouldReturnRightValueWhenEnvironmentOverridesProperties() {
        setenv(KEY, "7");
        assertThat(configuration.getBar(), is(equalTo(7)));
    }

    @Test
    public void shouldReturnRightValueWhenSystemPropertiesOverridesEnvironment() {
        setenv(KEY, "14");
        System.setProperty(KEY, "7");
        assertThat(configuration.getBar(), is(equalTo(7)));
    }

    @Test
    public void shouldIncludeFullKeyInException() {
        exception.expect(MissingConfigurationKeyException.class);
        exception.expectMessage(containsString("eg.baz"));
        configuration.getBaz();
    }
}
