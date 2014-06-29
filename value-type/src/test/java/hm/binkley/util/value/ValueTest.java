package hm.binkley.util.value;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ValueTest {
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @SuppressWarnings("ConstantConditions")
    @Test
    public void factoryMethodShouldNPE() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("value");
        // TODO: How to check call stack?

        TestyValue.of(null);
    }

    @Test
    public void shouldEqual() {
        assertThat(TestyValue.of(3), is(equalTo(TestyValue.of(3))));
    }

    @Test
    public void shouldCompare() {
        assertThat(TestyValue.of(3), comparesEqualTo(TestyValue.of(3)));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void compareShouldNPE() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("that");

        TestyValue.of(3).compareTo(null);
    }
}
