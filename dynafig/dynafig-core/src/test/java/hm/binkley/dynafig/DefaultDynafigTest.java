package hm.binkley.dynafig;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Properties;

import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@code DefaultDynafigTest} tests {@link DefaultDynafig}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley</a>
 */
public final class DefaultDynafigTest<T, R>
        extends DynafigTesting<T, R, DefaultDynafig> {
    public DefaultDynafigTest(final Args<T, R> args) {
        super(args);
    }

    @Before
    public void setUpFixture() {
        dynafig(new DefaultDynafig());
    }

    @Override
    protected void presetValue(final String value) {
        dynafig(new DefaultDynafig(singletonMap(KEY, value)));
    }

    public static final class CtorTest {
        @Rule
        public final ExpectedException thrown = ExpectedException.none();

        @Test
        public void shouldConstructFromMap() {
            assertThat(new DefaultDynafig(singletonMap(KEY, "")).
                    track(KEY).isPresent(), is(true));
        }

        @Test
        public void shouldConstructFromEntryStream() {
            assertThat(new DefaultDynafig(singletonMap(KEY, "").
                    entrySet().stream()).
                    track(KEY).isPresent(), is(true));
        }

        @Test
        public void shouldConstructFromProperties() {
            final Properties properties = new Properties();
            properties.put(KEY, "");

            assertThat(new DefaultDynafig(properties).
                    track(KEY).isPresent(), is(true));
        }
    }
}
