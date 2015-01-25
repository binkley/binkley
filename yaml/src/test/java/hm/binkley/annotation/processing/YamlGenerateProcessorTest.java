package hm.binkley.annotation.processing;

import hm.binkley.annotation.YamlGenerate;
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

@Deprecated // Check that unrelated annos do not croak the processor
@YamlGenerate(inputs = {"/foo/*.yml", "classpath:/bar/3.yml"},
        namespace = "fooby")
interface YamlGenerateTestClasses {}
