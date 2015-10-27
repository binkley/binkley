package hm.binkley.util;

import com.google.common.reflect.TypeToken;
import hm.binkley.util.Converter.DuplicateConversion;
import hm.binkley.util.Converter.UnsupportedConversion;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.List;

import static java.lang.String.format;
import static java.time.ZoneOffset.UTC;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@code ConverterTest} tests {@link Converter}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public class ConverterTest {
    private Converter converter;

    @Before
    public void setUp() {
        converter = new Converter();
    }

    @Test
    public void shouldConvertString()
            throws Exception {
        assertThat("bob",
                is(equalTo(converter.convert(String.class, "bob"))));
    }

    @Test
    public void shouldConvertRegistered()
            throws Exception {
        assertThat(Paths.get("/dev/null"),
                is(equalTo(converter.convert(Path.class, "/dev/null"))));
    }

    @Test
    public void shouldConvertWithParse()
            throws Exception {
        assertThat(LocalTime.parse("00:00"),
                is(equalTo(converter.convert(LocalTime.class, "00:00"))));
    }

    @Test
    public void shouldConvertWithValueOf()
            throws Exception {
        assertThat(3, is(equalTo(converter.convert(Integer.class, "3"))));
    }

    @Test
    public void shouldConvertWithOf()
            throws Exception {
        assertThat(UTC,
                is(equalTo(converter.convert(ZoneOffset.class, "Z"))));
    }

    @Test
    public void shouldConvertWithNew()
            throws Exception {
        assertThat(new File("/dev/null"),
                is(equalTo(converter.convert(File.class, "/dev/null"))));
    }

    @Test
    public void shouldConvertToResourceList()
            throws Exception {
        final String path = format("classpath:/%s.class",
                getClass().getName().replace('.', '/'));
        final List<Resource> resources = asList(
                new PathMatchingResourcePatternResolver().getResources(path));

        assertThat(resources, is(equalTo(converter
                .convert(new TypeToken<List<Resource>>() {}, path))));
    }

    @Test
    public void shouldRegister()
            throws Exception {
        converter.register(Year.class, value -> Year.of(0));

        assertThat(Year.of(0),
                is(equalTo(converter.convert(Year.class, "zero"))));
    }

    @Test
    @Ignore("Figure out parsing enough to test legitimately")
    public void shouldRegisterDate() {
    }

    @Test(expected = UnsupportedConversion.class)
    public void shouldThrowWhenUnsupported()
            throws Exception {
        converter.convert(Package.class, getClass().getPackage().getName());
    }

    @Test(expected = DuplicateConversion.class)
    public void shouldThrowWhenDuplicate() {
        converter.register(Path.class, Paths::get);
    }
}
