package hm.binkley.util.value;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * {@code ValueTest} tests {@link Value} and {@link ValueTypeProcessor}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public class ValueTest {
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @SuppressWarnings("ConstantConditions")
    @Test
    public void factoryMethodShouldNPE() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("value");
        // TODO: How to check call stack?

        IntTestyValue.of(null);
    }

    @Test
    public void shouldEqual() {
        assertThat(IntTestyValue.of(3), is(equalTo(IntTestyValue.of(3))));
    }

    @Test
    public void shouldCompare() {
        assertThat(IntTestyValue.of(3), comparesEqualTo(IntTestyValue.of(3)));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void compareShouldNPE() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("that");

        IntTestyValue.of(3).compareTo(null);
    }

    @Test
    public void shouldProcessDefaultMethods() {
        assertThat(IntTestyValue.of(3).return7(), is(equalTo(7)));
    }

    @Test
    public void shouldInternStringValues() {
        final String value = new String(new char[]{'a', 'b'});

        assertThat(StringTestyValue.of(value).get(), is(
                sameInstance(StringTestyValue.of("ab").get())));
    }
}
