package hm.binkley.annotation.processing;

import fooby.Howard;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.contrib.java.lang.system.StandardErrorStreamLog;
import org.junit.contrib.java.lang.system.StandardOutputStreamLog;
import org.junit.contrib.java.lang.system.TextFromStandardInputStream;

import javax.tools.JavaCompiler;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.Files.copy;
import static javax.tools.ToolProvider.getSystemJavaCompiler;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.contrib.java.lang.system.TextFromStandardInputStream.emptyStandardInputStream;

/**
 * {@code YamlGenerateProcessorTest} tests {@link YamlGenerateProcessor}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley</a>
 */
public final class YamlGenerateProcessorTest {
    @Rule
    public final TextFromStandardInputStream in = emptyStandardInputStream();
    @Rule
    public final StandardOutputStreamLog out = new StandardOutputStreamLog();
    @Rule
    public final StandardErrorStreamLog err = new StandardErrorStreamLog();
    @Rule
    public final RestoreSystemProperties sysprops
            = new RestoreSystemProperties();

    @Test
    public void shouldGenerate() {
        assertThat(new Howard().baz(), is(equalTo("stringy!")));
    }

    @Test
    public void shouldFailOnBadParent()
            throws IOException {
        final Path root = Files.createTempDirectory("test");
        root.toFile().deleteOnExit();

        copy(getClass().getResourceAsStream("/tests/bad-parent.yml"),
                root.resolve("bad-parent.yml"));
        copy(getClass().getResourceAsStream("/tests/bad-parent.java"),
                root.resolve("bad-parent.java"));

        final JavaCompiler javac = getSystemJavaCompiler();
        assertThat(javac.run(System.in, System.out, System.err, "Tests"),
                is(equalTo(0)));
    }
}
