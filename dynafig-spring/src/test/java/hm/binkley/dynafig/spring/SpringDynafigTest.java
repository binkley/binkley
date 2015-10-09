package hm.binkley.dynafig.spring;

import hm.binkley.dynafig.DynafigTesting;
import org.junit.Before;
import org.springframework.core.env.Environment;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@code SpringDynafigTest} tests {@link SpringDynafig}.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">B. K. Oxley</a>
 */
public final class SpringDynafigTest<T, R>
        extends DynafigTesting<T, R, SpringDynafig> {
    private final Environment env = mock(Environment.class);

    public SpringDynafigTest(final Args<T, R> args) {
        super(args);
    }

    @Before
    public void setUpFixture() {
        dynafig(new SpringDynafig(env));
    }

    @Override
    protected void presetValue(final String value) {
        when(env.containsProperty(eq(KEY))).thenReturn(true);
        when(env.getProperty(eq(KEY))).thenReturn(value);
    }
}
