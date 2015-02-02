package hm.binkley.annotation.processing;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@code YamlGenerateProcessorTest} tests {@link YamlGenerateProcessor}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley</a>
 */
public final class YamlGenerateProcessorTest {
    @Test
    public void shouldGenerate() {
        assertThat(new fooby.Howard().baz(), is(equalTo("stringy!")));
    }
}
