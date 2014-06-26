package hm.binkley.util;

import hm.binkley.util.XProperties.FailedConversionException;
import hm.binkley.util.XProperties.MissingPropertyException;
import hm.binkley.util.XProperties.RecursiveIncludeException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.core.io.Resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.newOutputStream;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public final class XPropertiesTest {
    private static final String pathPrefix = "/" + XPropertiesTest.class.getPackage().getName()
            .replaceAll("\\.", "/");

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private XProperties xprops;

    @Before
    public void setUp() {
        xprops = new XProperties();
    }

    @Test
    public void shouldInclude()
            throws IOException {
        xprops.load(new StringReader(format("#include %s/included.properties", pathPrefix)));

        assertThat(xprops.getProperty("bar"), is(equalTo("found")));
    }

    @Test
    public void shouldNoticeRecursiveInclude()
            throws IOException {
        thrown.expect(RecursiveIncludeException.class);
        thrown.expectMessage(containsString(format("%s/tweedle-dee.properties", pathPrefix)));

        XProperties.from(format("%s/tweedle-dee.properties", pathPrefix));
    }

    @Test
    public void shouldReplaceInclude()
            throws IOException {
        xprops.setProperty("foo", "util");
        xprops.load(new StringReader(format("#include %s/${foo}/included.properties",
                removeLastPathComponent(pathPrefix))));

        assertThat(xprops.getProperty("bar"), is(equalTo("found")));
    }

    @Test
    public void shouldIncludeResource()
            throws IOException {
        final Path tmp = createTempFile("test", ".tmp");
        try (final OutputStream out = newOutputStream(tmp)) {
            new PrintStream(out).println("bar=found");
            xprops.setProperty("tmp", tmp.toString());
            xprops.load(new StringReader("#include file:${tmp}"));

            assertThat(xprops.getProperty("bar"), is(equalTo("found")));
        } finally {
            delete(tmp);
        }
    }

    @Test
    public void shouldIncludeMultiple()
            throws IOException {
        xprops.load(new StringReader(
                format("#include %1$s/included.properties, %1$s/included2.properties",
                        pathPrefix)));

        assertThat(xprops.getProperty("foo"), is(equalTo("found")));
    }

    @Test
    public void shouldIncludeWildcard()
            throws IOException {
        xprops.load(new StringReader(format("#include %s/included*.properties", pathPrefix)));

        assertThat(xprops.getProperty("foo"), is(equalTo("found")));
    }

    @Test
    public void shouldThrowOnBadInclude()
            throws IOException {
        thrown.expect(FileNotFoundException.class);
        thrown.expectMessage(containsString("no-such-location"));

        xprops.load(new StringReader("#include no-such-location"));
    }

    @Test
    public void shouldReplaceGetProperty() {
        xprops.setProperty("foo", "found");
        xprops.setProperty("bar", "${foo}");

        assertThat(xprops.getProperty("bar"), is(equalTo("found")));
    }

    @Test
    public void shouldReplaceRepeatedGetProperty() {
        xprops.setProperty("baz", "found");
        xprops.setProperty("foo", "${baz}");
        xprops.setProperty("bar", "${foo}");

        assertThat(xprops.getProperty("bar"), is(equalTo("found")));
    }

    @Test
    public void shouldReplaceNestedGetProperty() {
        xprops.setProperty("baz", "found");
        xprops.setProperty("foo", "baz");
        xprops.setProperty("bar", "${${foo}}");

        assertThat(xprops.getProperty("bar"), is(equalTo("found")));
    }

    @Test
    public void shouldReplaceGet() {
        xprops.setProperty("foo", "found");
        xprops.setProperty("bar", "${foo}");

        assertThat(xprops.get("bar"), is(equalTo((Object) "found")));
    }

    @Test
    public void shouldReplaceSystemProperty() {
        xprops.setProperty("bar", "${user.name}");

        assertThat(xprops.getProperty("bar"), is(not(nullValue())));
    }

    @Test
    public void shouldReplaceEnvironmentVariables() {
        xprops.setProperty("bar", isWindows() ? "${USERNAME}" : "${USER}");

        assertThat(xprops.getProperty("bar"), is(not(nullValue())));
    }

    @Test
    public void shouldThrowOnMissingReplacement() {
        thrown.expect(MissingPropertyException.class);
        thrown.expectMessage(containsString("no-such-value"));

        xprops.setProperty("missing", "${no-such-value}");

        xprops.getProperty("missing");
    }

    @Test
    public void shouldThrowWhenWronglyKeyAssignedToNonString() {
        thrown.expect(ClassCastException.class);
        thrown.expectMessage(containsString(Integer.class.getName()));

        xprops.setProperty("bar", "");

        // Ignored but required to force type conversion
        final Integer ignored = xprops.getObject("bar");
    }

    @Test
    public void shouldRegister() {
        XProperties.register("x", String::toString);
        xprops.setProperty("bar", "foo");

        assertThat(xprops.getObject("x:bar"), is(equalTo("foo")));
    }

    @Test
    public void shouldGetInt() {
        xprops.setProperty("bar", "3");

        assertThat(xprops.getObject("int:bar"), is(equalTo(3)));
    }

    @Test
    public void shouldGetIntegerExplicit() {
        xprops.setProperty("bar", "3");

        assertThat(xprops.getObject("java.math.BigInteger:bar"), is(equalTo(new BigInteger("3"))));
    }

    @Test
    public void shouldGetIntegerAliased() {
        xprops.setProperty("bar", "3");

        assertThat(xprops.getObject("integer:bar"), is(equalTo(new BigInteger("3"))));
    }

    @Test
    public void shouldGetDecimal() {
        xprops.setProperty("bar", "3");

        assertThat(xprops.getObject("number:bar"), is(equalTo(new BigDecimal("3"))));
    }

    @Test
    public void shouldGetResources() {
        xprops.setProperty("bar",
                format("classpath*:%s/**/included*.properties", firstPathComponent(pathPrefix)));
        final List<Resource> resources = xprops.getObject("resource*:bar");

        assertThat(resources.size(), is(equalTo(2)));
        assertThat(resources.get(0).getFilename(), endsWith("included.properties"));
        assertThat(resources.get(1).getFilename(), endsWith("included2.properties"));
    }

    @Test
    public void shouldGetAddress() {
        xprops.setProperty("bar", "[2001:db8::1]:80");

        final InetSocketAddress address = xprops.getObject("address:bar");
        assertThat(address.getHostString(), is(equalTo("2001:db8::1")));
        assertThat(address.getPort(), is(equalTo(80)));
    }

    @Test
    public void shouldThrowForBadAddress() {
        thrown.expect(FailedConversionException.class);
        thrown.expectMessage(containsString("address:bar"));
        thrown.expectMessage(containsString("-@-"));

        xprops.setProperty("bar", "-@-");

        xprops.getObject("address:bar");
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    private static String removeLastPathComponent(final String path) {
        return path.replaceAll("/[^/]+$", "");
    }

    private static String firstPathComponent(final String path) {
        final Matcher matcher = Pattern.compile("^(/[^/]+).*").matcher(path);
        if (!matcher.find())
            fail();
        return matcher.group(1);
    }
}
