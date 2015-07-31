package hm.binkley.net.http;

import com.squareup.okhttp.HttpUrl;
import lombok.experimental.ExtensionMethod;

import static java.lang.String.format;
import static java.lang.System.out;
import static java.util.stream.Collectors.joining;

/**
 * {@code HttpUrlExtensionsMain} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation
 */
@ExtensionMethod(HttpUrlExtensions.class)
public final class HttpUrlExtensionsMain {
    public static void main(final String... args) {
        final HttpUrl url = HttpUrl
                .parse("http://%3a:X@foo/%3b/bar;a=2,3;a=4;b%3bc;c/baz/qux;d/%3ba/?p=1,2&p=3&q&q");
        out.println("url = " + url);
        out.println("username = " + url.username());
        out.println("password = " + url.password());
        out.println("paths = " + url.pathSegments().stream().
                collect(joining("' | '", "[ '", "' ]")));
        out.println(url.queryParameterNames().stream().
                map(name -> format("%s=%s", name,
                        url.queryParameterValues(name))).
                collect(joining(", ", "queries = {", "}")));

        out.println(url.pathSegmentMap());
        out.println(url.queryParameterMap());
    }
}
