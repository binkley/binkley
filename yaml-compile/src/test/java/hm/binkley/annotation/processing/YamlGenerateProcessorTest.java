package hm.binkley.annotation.processing;

import fooby.Howard;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.contrib.java.lang.system.StandardErrorStreamLog;
import org.junit.contrib.java.lang.system.StandardOutputStreamLog;
import org.junit.contrib.java.lang.system.TextFromStandardInputStream;

import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static com.google.common.base.Joiner.on;
import static java.lang.System.arraycopy;
import static java.lang.System.getProperty;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createTempDirectory;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static javax.tools.ToolProvider.getSystemJavaCompiler;
import static org.hamcrest.Matchers.containsString;
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
        final Path root = createTempDirectory("test");
        root.toFile().deleteOnExit();
        createDirectories(root);

        final Path packaj = root.resolve("tests");
        packaj.toFile().mkdir();
        copy(getClass().getResourceAsStream("/tests/BadParent.yml"),
                packaj.resolve("BadParent.yml"));
        final Path java = packaj.resolve("BadParent.java");
        copy(getClass().getResourceAsStream("/tests/BadParent.java"), java);

        assertThat(err.getLog(), compile(root, java.toString()),
                is(equalTo(1)));
        assertThat(err.getLog(),
                containsString("Undefined parent for 'NoSuchParent YaYa'"));
    }

    private static final String ps = getProperty("path.separator");
    private static final Pattern sep = Pattern.compile(ps);

    private static int compile(final Path root, final String... args) {
        final String tmpdir = getProperty("java.io.tmpdir");
        final String[] xArgs = new String[4 + args.length];
        xArgs[0] = "-classpath";
        xArgs[1] = on(ps).join(root,
                asList(sep.split(getProperty("java.class.path"))).stream().
                        filter(p -> !p.equals(tmpdir)).
                        collect(joining(ps)));
        xArgs[2] = "-d";
        xArgs[3] = root.toString();
        arraycopy(args, 0, xArgs, 4, args.length);
        return getSystemJavaCompiler().
                run(System.in, System.out, System.err, xArgs);
    }
}
