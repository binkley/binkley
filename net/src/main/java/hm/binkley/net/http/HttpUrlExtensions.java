package hm.binkley.net.http;

import com.squareup.okhttp.HttpUrl;
import lombok.EqualsAndHashCode;
import lombok.experimental.ExtensionMethod;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Arrays.spliterator;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.function.Function.identity;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;
import static java.util.stream.StreamSupport.stream;

/**
 * {@code HttpUrlExtensions} is a Lombok <a href="https://projectlombok.org/features/experimental/ExtensionMethod.html">method
 * extension</a> for {@link HttpUrl} from <a href="http://square.github.io/okhttp/">OkHttp</a>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation
 */
public final class HttpUrlExtensions {
    /** Do not instantiate; only used by Lombok's {@link ExtensionMethod}. */
    private HttpUrlExtensions() {
    }

    /**
     * Creates a list of path segments for the given <var>url</var>, each
     * segment being a mapping of a named path component to any path
     * parameters (matrix parameters).
     *
     * @see <a href="http://tools.ietf.org/html/std66#section-3.3" title="STD
     * 66 - Uniform Resource Identifier (URI): Generic Syntax, &sect;3.3
     * Path"><cite>STD 66 - Uniform Resource Identifier (URI): Generic Syntax,
     * &sect;3.3 Path</cite></a>
     * @see <a href="http://doriantaylor.com/policy/http-url-path-parameter-syntax"
     * title="HTTP URL Path Parameter Syntax"><cite>HTTP URL Path Parameter
     * Syntax</cite></a>
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
        private final Map<String, List<String>> parameters;

        /** Package scope for testing. */
        PathSegment(final int position, final String name,
                final Map<String, List<String>> parameters) {
            this.position = position;
            this.name = name;
            this.parameters = parameters;
        }

        /** @todo Dirty code */
        private PathSegment(final int position, final String segment) {
            this.position = position;

            final String[] pathAndPairs = semicolon.split(segment);
            if (0 == pathAndPairs.length) {
                name = segment;
                parameters = emptyMap();
                return;
            }
            name = pathAndPairs[0];

            final String method = System.getenv("method");
            switch (null == method ? "middle" : method) {
            case "fancy":
                System.out.println("Fancy");
                parameters = crazyComplicatedWay(pathAndPairs);
                break;
            default:
                System.out.println("Middle");
                final Map<String, List<String>> parameters
                        = new LinkedHashMap<>();
                streamPairs(pathAndPairs).
                        map(PathSegment::makePair).
                        forEach(addTo(parameters));
                parameters.entrySet().stream().
                        forEach(PathSegment::unmodifiableValue);
                this.parameters = unmodifiableMap(parameters);
                break;
            case "simple":
                System.out.println("Simple");
                throw new UnsupportedOperationException(
                        "NEEDS IMPLEMENTATION");
            }
        }

        @Nonnull
        private static Map<String, List<String>> crazyComplicatedWay(
                final String[] pathAndPairs) {
            return unmodifiableMap(streamPairs(pathAndPairs).
                    map(PathSegment::makePair).
                    collect(groupingBy(pair -> pair[0])).
                    entrySet().stream().
                    map(e -> new SimpleImmutableEntry<>(e.getKey(),
                            e.getValue().stream().
                                    map(v -> 2 == v.length ? v[1] : null).
                                    collect(collectingAndThen(toList(),
                                            (Function<List<String>, List<String>>) list -> unmodifiableList(
                                                    list))))).
                    collect(toMap(Entry::getKey, Entry::getValue)));
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
            return parameters;
        }

        @Override
        public String toString() {
            return format("[%d]%s=%s", position, name, parameters);
        }

        @Nonnull
        private static Stream<String> streamPairs(
                final String[] pathAndPairs) {
            return stream(spliterator(pathAndPairs, 1, pathAndPairs.length),
                    false);
        }

        private static String[] makePair(final String pair) {
            return equals.split(pair, 2);
        }

        @Nonnull
        private static Consumer<String[]> addTo(
                final Map<String, List<String>> parameters) {
            return pair -> parameters.
                    computeIfAbsent(pair[0], k -> new ArrayList<>()).
                    add(2 == pair.length ? pair[1] : null);
        }

        private static void unmodifiableValue(
                final Entry<String, List<String>> pair) {
            pair.setValue(unmodifiableList(pair.getValue()));
        }
    }
}
