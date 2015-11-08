package hm.binkley.annotation.processing;

import fooby.Howard;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.contrib.java.lang.system.TextFromStandardInputStream;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import static hm.binkley.util.Arrays.array;
import static java.lang.System.err;
import static java.lang.System.getProperty;
import static java.lang.System.in;
import static java.lang.System.out;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createTempDirectory;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static javax.tools.ToolProvider.getSystemJavaCompiler;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.contrib.java.lang.system.TextFromStandardInputStream.emptyStandardInputStream;

public final class YamlGenerateProcessorTest {
    @Rule
    public final TextFromStandardInputStream sin = emptyStandardInputStream();
    @Rule
    public final SystemOutRule sout = new SystemOutRule().
            enableLog().
            mute();
    @Rule
    public final SystemErrRule serr = new SystemErrRule().
            enableLog().
            mute();
    @Rule
    public final RestoreSystemProperties sysprops
            = new RestoreSystemProperties();

    @After
    public void tearDown() {
        sout.clearLog();
        serr.clearLog();
    }

    @Test
    public void shouldGenerate() {
        assertThat(new Howard().baz(), is(equalTo("stringy!")));
    }

    @Test
    public void shouldBeGood()
            throws IOException {
        final Path root = createTempDirectory("test");
        root.toFile().deleteOnExit();
        createDirectories(root);

        final Path packaj = root.resolve("tests");
        assertThat(packaj.toFile().mkdir(), is(true));
        final Path yml = packaj.resolve("Good.yml");
        copy(getClass().getResourceAsStream("/tests/Good.yml"), yml);
        final Path generate = packaj.resolve("GoodClasses.java");
        copy(getClass().getResourceAsStream("/tests/GoodClasses.java"),
                generate);

        assertThat(compile(root, generate), is(equalTo(0)));
        sout.clearLog();
        serr.clearLog();

        final Path test = packaj.resolve("GoodTest.java");
        copy(getClass().getResourceAsStream("/tests/GoodTest.java"), test);
        assertThat(compile(root, test), is(equalTo(0)));
    }

    @Test
    public void shouldFailOnBadParent()
            throws IOException {
        final Path root = createTempDirectory("test");
        root.toFile().deleteOnExit();
        createDirectories(root);

        final Path packaj = root.resolve("tests");
        assertThat(packaj.toFile().mkdir(), is(true));
        final Path yml = packaj.resolve("BadParent.yml");
        copy(getClass().getResourceAsStream("/tests/BadParent.yml"), yml);
        final Path generate = packaj.resolve("BadParentClasses.java");
        copy(getClass().getResourceAsStream("/tests/BadParentClasses.java"),
                generate);

        assertThat(compile(root, generate), is(equalTo(1)));
        assertThat(serr.getLog(),
                containsString("Undefined parent for 'NoSuchParent YaYa'"));
    }

    private static final String ps = getProperty("path.separator");
    private static final Pattern sep = Pattern.compile(ps);

    private static int compile(final Path root, final Path source) {
        final Collection<String> xArgs = new ArrayList<>();
        xArgs.add("-classpath");
        xArgs.add(classpath(root));
        asList("-sourcepath", "-d", "-s").stream().
                forEach(f -> {
                    xArgs.add(f);
                    xArgs.add(root.toString());
                });
        xArgs.add(source.toString());

        return getSystemJavaCompiler().
                run(in, out, err, array(xArgs, String.class));
    }

    private String log() {
        final String err = serr.getLog();
        return isEmpty(err) ? sout.getLog() : err;
    }

    private <T> void assertThat(final T actual,
            final Matcher<? super T> matcher) {
        MatcherAssert.assertThat(log(), actual, matcher);
    }

    private static String classpath(final Path root) {
        return root + ps +
                asList(sep.split(getProperty("java.class.path"))).stream().
                        filter(p -> !p.equals(getProperty("java.io.tmpdir"))).
                        collect(joining(ps));
    }
}
