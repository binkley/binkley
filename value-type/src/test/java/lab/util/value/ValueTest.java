package lab.util.value;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ValueTest {
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldEqual() {
        assertThat(TestyValue.of(3), is(equalTo(TestyValue.of(3))));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void factoryMethodShouldNPE() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("value");
        // TODO: How to check call stack?

        TestyValue.of(null);
    }
}
