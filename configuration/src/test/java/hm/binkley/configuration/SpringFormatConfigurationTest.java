package hm.binkley.configuration;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@code SpringFormatConfigurationTest} tests {@link SpringFormatConfiguration}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public class SpringFormatConfigurationTest {
    private static final String KEY = "eg.bar";
    private EgSpringConfiguration configuration;

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
    @Ignore("How to test with putenv?")
    public void shouldReturnRightValueWhenEnvironmentOverridesProperties() {
        System.getenv().put(KEY, "7");
        assertThat(configuration.getBar(), is(equalTo(7)));
    }

    @Test
    @Ignore("How to test with putenv?")
    public void shouldReturnRightValueWhenSystemPropertiesOverridesEnvironment() {
        System.getenv().put(KEY, "14");
        System.setProperty(KEY, "7");
        assertThat(configuration.getBar(), is(equalTo(7)));
    }
}
