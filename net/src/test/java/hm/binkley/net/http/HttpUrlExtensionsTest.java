package hm.binkley.net.http;

import com.squareup.okhttp.HttpUrl;
import hm.binkley.net.http.HttpUrlExtensions.PathSegment;
import lombok.experimental.ExtensionMethod;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@code HttpUrlExtensionsTest} tests {@link HttpUrlExtensions}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
@RunWith(Parameterized.class)
public class HttpUrlExtensionsTest {
    private static final PathSegment emptyRoot = new PathSegment(0, "",
            emptyMap());

    @Parameter(0)
    public String url;
    @Parameter(1)
    public List<PathSegment> paths;
    @Parameter(2)
    public Map<String, List<String>> queries;

    @Parameters(name = "{index}: {0} -> {1} ; {2}")
    public static Collection<Object[]> parameters() {
        return asList(args("http://foo/", emptyMap(), emptyRoot),
                args("http://foo/bar", emptyMap(),
                        new PathSegment(0, "bar", emptyMap())),
                args("http://foo/?a&a", singletonMap("a", asList(null, null)),
                        emptyRoot));
    }

    @Test
    public void shouldParseUrl() {
        final X x = new X(url);

        assertThat(x.paths(), is(equalTo(paths)));
        assertThat(x.queries(), is(equalTo(queries)));
    }

    @ExtensionMethod(HttpUrlExtensions.class)
    public static class X {
        private final HttpUrl url;

        public X(final String url) {
            this.url = HttpUrl.parse(url);
        }

        public List<PathSegment> paths() {
            return url.pathSegmentMap();
        }

        public Map<String, List<String>> queries() {
            return url.queryParameterMap();
        }
    }

    private static Object[] args(final String url,
            final Map<String, List<String>> queries,
            final PathSegment... paths) {
        return new Object[]{url, asList(paths), queries};
    }
}
