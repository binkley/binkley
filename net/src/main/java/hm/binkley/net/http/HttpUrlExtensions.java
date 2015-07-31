package hm.binkley.net.http;

import com.squareup.okhttp.HttpUrl;
import lombok.EqualsAndHashCode;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.function.Function.identity;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;

/**
 * {@code HttpUrlExtensions} is a Lombok <a href="https://projectlombok.org/features/experimental/ExtensionMethod.html">method
 * extension</a> for {@link HttpUrl} from <a href="http://square.github.io/okhttp/">OkHttp</a>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation
 */
public final class HttpUrlExtensions {
    /**
     * Creates a list of path segments for the given <var>url</var>, each
     * segment being a mapping of a named path component to any path
     * parameters (matrix parameters).
     */
    @Nonnull
    public static List<PathSegment> pathSegmentMap(
            @Nonnull final HttpUrl url) {
        final List<String> segments = url.pathSegments();
        final List<PathSegment> pathSegments = new ArrayList<>(
                segments.size());
        return range(0, segments.size()).
                mapToObj(i -> new PathSegment(i, segments.get(i))).
                collect(toCollection(() -> pathSegments));
    }

    /**
     * Creates a map of query parameters for the given <var>url</var>,
     * iterable in the same order the parameters appear in the URL.
     */
    @Nonnull
    public static Map<String, List<String>> queryParameterMap(
            @Nonnull final HttpUrl url) {
        final Set<String> names = url.queryParameterNames();
        final Map<String, List<String>> queries = new LinkedHashMap<>(
                names.size());
        return unmodifiableMap(names.stream().
                collect(toMap(identity(), url::queryParameterValues,
                        (a, b) -> {
                            throw new AssertionError();
                        }, () -> queries)));
    }

    @EqualsAndHashCode
    public static final class PathSegment {
        private static final Pattern semicolon = compile(";");
        private static final Pattern equals = compile("=");

        private final int position;
        private final String name;
        private final Map<String, List<String>> params;

        /** Package scope for testing. */
        PathSegment(final int position, final String name,
                final Map<String, List<String>> params) {
            this.position = position;
            this.name = name;
            this.params = params;
        }

        private PathSegment(final int position, final String segment) {
            this.position = position;
            final List<String> lumps = asList(semicolon.split(segment));

            if (lumps.isEmpty()) {
                name = segment;
                params = emptyMap();
                return;
            }

            name = lumps.get(0);

            final Map<String, List<String>> params = new LinkedHashMap<>();
            lumps.subList(1, lumps.size()).stream().
                    map(l -> equals.split(l, 2)).
                    forEach(p -> params.
                            computeIfAbsent(p[0], k -> new ArrayList<>()).
                            add(2 == p.length ? p[1] : null));
            params.entrySet().stream().
                    forEach(e -> e.setValue(unmodifiableList(e.getValue())));
            this.params = unmodifiableMap(params);
        }

        /** The position of this path segment in the URL, 0-based. */
        @Nonnegative
        public int position() {
            return position;
        }

        /** The "name" or path component of this segment in the URL. */
        @Nonnull
        public String name() {
            return name;
        }

        /**
         * The unmodifiable map of path parameters for this path segment,
         * iterable in the same order as the path segments appear in the URL.
         * Similarly, path parameters iterate in their order of appearance.
         */
        @Nonnull
        public Map<String, List<String>> params() {
            return params;
        }

        @Override
        public String toString() {
            return format("[%d]%s=%s", position, name, params);
        }
    }
}
