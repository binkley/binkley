package hm.binkley.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.net.HostAndPort;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Throwables.getRootCause;
import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.net.InetSocketAddress.createUnresolved;
import static java.util.Arrays.asList;
import static java.util.regex.Pattern.compile;

/**
 * {@code XProperties} is a {@code java.util.Properties} supporting inclusion of other properties
 * files, parameter substition and typed return values.
 *
 * @see #load(Reader) loading properties with inclusions
 * @see #getProperty(String) getting properties with substitution
 * @todo Using cache for conversions assumes constant properties; is this correct?
 */
public class XProperties
        extends Properties {
    private static final Pattern include = compile("^#include\\s+(.*)\\s*$");
    private static final Pattern comma = compile("\\s*,\\s*");
    private static final Pattern colon = compile(":");
    private static final Map<String, Conversion> factories = new ConcurrentHashMap<>();
    static {
        register("address", value -> {
            final HostAndPort parsed = HostAndPort.fromString(value).requireBracketsForIPv6();
            return createUnresolved(parsed.getHostText(), parsed.getPort());
        });
        register("bundle", ResourceBundle::getBundle);
        register("byte", Byte::valueOf);
        register("class", Class::forName);
        register("date", LocalDate::parse);
        register("double", Double::valueOf);
        register("duration", Duration::parse);
        register("file", File::new);
        register("float", Float::valueOf);
        register("inet", InetAddress::getByName);
        register("int", Integer::valueOf);
        register("integer", BigInteger::new);
        register("long", Long::valueOf);
        register("number", BigDecimal::new);
        register("path", Paths::get);
        register("period", Period::parse);
        register("regex", Pattern::compile);
        register("resource",
                value -> new DefaultResourceLoader(currentThread().getContextClassLoader())
                        .getResource(value));
        register("resource*", value -> asList(new PathMatchingResourcePatternResolver(
                currentThread().getContextClassLoader()).getResources(value)));
        register("short", Short::valueOf);
        register("time", LocalDateTime::parse);
        register("timestamp", Instant::parse);
        register("tz", TimeZone::getTimeZone);
        register("uri", URI::create);
        register("url", URL::new);
    }

    private final LoadingCache<Key, Object> converted = CacheBuilder.newBuilder().
            build(new Converted());

    private final StrSubstitutor substitutor = new StrSubstitutor(new FindValue());

    {
        substitutor.setEnableSubstitutionInVariables(true);
    }

    /**
     * Registers a new alias mapping for converting property values to typed objects using the given
     * <var>prefix</var> and <var>factory</var>.
     * <p>
     * The <var>type</var> prefix represents a Java or JDK type as listed here:
     * <table><tr><th>Prefix</th> <th>Type</th></tr> <tr><td>address</td> <td>{@code
     * java.net.InetSocketAddress}</td></tr> <tr><td>bundle</td> <td>{@code
     * java.util.ResourceBundle}</td></tr> <tr><td>byte</td> <td>{@code java.lang.Byte}</td></tr>
     * <tr><td>class</td> <td>java.lang.Class</td></tr> <tr><td>date</td> <td>{@code
     * java.time.LocalDate}</td></tr> <tr><td>double</td> <td>{@code java.lang.Double}</td></tr>
     * <tr><td>duration</td> <td>{@code java.time.Duration}</td></tr> <tr><td>file</td> <td>{@code
     * java.io.File}</td></tr> <tr><td>float</td> <td>{@code java.lang.Float}</td></tr>
     * <tr><td>inet</td> <td>{@code java.net.InetAddress}</td></tr> <tr><td>int</td> <td>{@code
     * java.lang.Integer}</td></tr> <tr><td>integer</td> <td>{@code java.math.BigInteger}</td></tr>
     * <tr><td>long</td> <td>{@code java.lang.Long}</td></tr> <tr><td>number</td> <td>{@code
     * java.math.BigDecimal}</td></tr> <tr><td>path</td> <td>{@code java.nio.file.Path}</td></tr>
     * <tr><td>period</td> <td>{@code java.time.Period}</td></tr> <tr><td>resource</td>
     * <tr><td>regex</td> <td>{@code java.util.regex.Pattern}</td></tr> <td>{@code
     * org.springframework.core.io.Resource}</td></tr> <tr><td>resource*</td> <td>{@code
     * java.util.List&lt;org.springframework.core.io.Resource&gt;}</td></tr> <tr><td>short</td>
     * <td>{@code java.lang.Short}</td></tr> <tr><td>time</td> <td>{@code
     * java.time.LocalDateTime}</td></tr> <tr><td>timestamp</td> <td>{@code
     * java.time.Instant}</td></tr> <tr><td>tz</td> <td>{@code java.util.TimeZone}</td></tr>
     * <tr><td>uri</td> <td>{@code java.net.URI}</td></tr> <tr><td>url</td> <td>{@code
     * java.net.URL}</td></tr> </table>
     *
     * @param prefix the alias prefix, never missing
     * @param factory the factory, never missing
     *
     * @throws DuplicatePropertyException if <var>prefix</var> is already registered
     * @see #getObject(String)
     */
    public static void register(@Nonnull final String prefix,
            @Nonnull final Conversion<?, ?> factory)
            throws DuplicatePropertyException {
        if (null != factories.putIfAbsent(prefix, factory))
            throw new DuplicatePropertyException(prefix);
    }

    /**
     * {@inheritDoc} <p/>
     * Processes lines of the form: <pre>
     * #include <var>Spring style resource path</var></pre> for inclusion.  Looks up <var>resource
     * path</var> with a {@link ResourcePatternResolver}; loading found resources in turn. Similarly
     * processes resource recursively for further inclusion.  Includes multiple resources separated
     * by comma (",") in the same "#include" statement.
     * <p>
     * Substitutes in resource paths of the
     * form: <pre>
     * #include ${<var>variable</var>}</pre> or portions thereof.  Looks up <var>variable</var> in
     * the current properties, and if not found, in the system properties.  If found, replaces the
     * text with the variable value, including "${" and trailing "}".
     * <p>
     * Use forward slashes ("/") only for path separators; <strong>do not</strong> use back slashes
     * ("\").
     * <p>
     * Examples: <table><tr><th>Text</th> <th>Result</th></tr> <tr><td>{@code #include
     * foo/file.properties}</td> <td>Includes {@code foo.properties} found in the
     * classpath</td></tr> <tr><td>{@code #include foo.properties, bar.properties}</td> <td>Includes
     * both {@code foo.properties} and {@code bar.properties}</td></tr> <tr><td>{@code #include
     * file:/var/tmp/${user.name }/foo.properties}</td> <td>Includes {@code foo.properties} found in
     * a directory named after the current user</td></tr> <tr><td>{@code #include
     * classpath*:**&#47;foo.properties}</td> <td>Includes all {@code foo.properties}</tr> files
     * found subdirectories of the classpath</td></tr> </table>
     *
     * @throws IOException if the properties cannot be loaded or if included resources cannot be
     * read
     * @see PathMatchingResourcePatternResolver
     * @see StrSubstitutor
     */
    @Override
    public synchronized void load(@Nonnull final Reader reader)
            throws IOException {
        final ResourcePatternResolver loader = new PathMatchingResourcePatternResolver(
                currentThread().getContextClassLoader());
        try (final CharArrayWriter writer = new CharArrayWriter()) {
            try (final BufferedReader lines = new BufferedReader(reader)) {
                for (String line = lines.readLine(); null != line; line = lines.readLine()) {
                    writer.append(line).append('\n');
                    final Matcher matcher = include.matcher(line);
                    if (matcher.matches())
                        for (final String x : comma.split(substitutor.replace(matcher.group(1))))
                            for (final Resource resource : loader.getResources(x))
                                try (final InputStream in = resource.getInputStream()) {
                                    load(in);
                                }
                }
            }

            super.load(new CharArrayReader(writer.toCharArray()));
        }
    }

    /**
     * {@inheritDoc} <p/>
     * Substitutes in property values sequences of the form: <pre>
     * ${<var>variable</var>}</pre>.  Looks up <var>variable</var> in the current properties, and
     * if
     * not found, in the system properties.  If found, replaces the text with the variable value,
     * including "${" and trailing "}".
     *
     * @throws MissingPropertyException if substitution refers to a missing property
     * @see StrSubstitutor
     */
    @Nullable
    @Override
    public String getProperty(final String key) {
        final String value = super.getProperty(key);
        if (null == value)
            return null;
        return substitutor.replace(value);
    }

    /**
     * {@inheritDoc} <p/>
     * Substitutes in string values sequences of the form: <pre>
     * ${<var>variable</var>}</pre> for substition.  Looks up <var>variable</var> in the current
     * properties, and if not found, in the system properties.  If found, replaces the text with the
     * variable value, including "${" and trailing "}".
     *
     * @see StrSubstitutor
     */
    @Nullable
    @Override
    public synchronized Object get(final Object key) {
        final Object value = super.get(key);
        if (null == value || !(value instanceof String))
            return value;
        return substitutor.replace((String) value);
    }

    /**
     * Gets a typed property value.  <strong>Responsibility is the caller's</strong> to assign the
     * return to a correct type; failure to do so will cause a {@code ClassCastException} at
     * run-time.
     * <p>
     * Typed keys are of the form: {@code <var>type</var>:<var>key</var>}.  The <var>key</var>
     * is the same key as {@link System#getProperty(String) System.getProperty}.  See {@link
     * #register(String, Conversion) register} for built-in <var>type</var> key prefixes.
     * <p>
     * Examples: <table><tr><th>Code</th> <th>Comment</th></tr> <tr><td>{@code Integer foo =
     * xprops.getObject("int:foo");}</td> <td>Gets the "foo" property as an possibly {@code null}
     * integer</td></tr> <tr><td>{@code int foo = xprops.getObject("int:foo");}</td> <td>Gets the
     * "foo" property as a primitive integer, throwing {@code NullPointerException} if
     * missing</td></tr> <tr><td>{@code Long foo = xprops.getObject("long:foo");}</td> <td>Gets the
     * "foo" property as an possibly {@code null} long; this is the same property as the previous
     * examples</td></tr></table>
     *
     * @param key the type-prefixed key, never missing
     * @param <T> the value type
     *
     * @return the type property value, possibly missing
     *
     * @see #getObjectOrDefault(String, Object)
     * @see #register(String, Conversion)
     */
    @Nullable
    public <T> T getObject(@Nonnull final String key) {
        return getObjectOrDefault(key, null);
    }

    /** @todo Javadoc */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getObjectOrDefault(@Nonnull final String key, final T defaultValue) {
        try {
            return (T) converted.get(new Key(key, defaultValue));
        } catch (final ExecutionException | UncheckedExecutionException e) {
            final String[] parts = colon.split(key, 2);
            throw new FailedConversionException(key, getProperty(parts[1]), e.getCause());
        }
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    private static <T, E extends Exception> Conversion<T, E> factoryFor(final String type) {
        // Look for alias first
        Conversion<T, E> factory = factories.get(type);
        if (null != factory)
            return factory;
        final Class<T> token = XProperties.<T>tokenFor(type);
        // Try Type.valueOf
        factory = invokeValueOf(token);
        if (null != factory)
            return factory;
        // Try Type.of
        factory = invokeOf(token);
        if (null != factory)
            return factory;
        // Try new Type
        factory = invokeConstructor(token);
        if (null != factory)
            return factory;
        throw new Bug("Unsupported conversion: %s", type);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static <T, E extends Exception> Conversion<T, E> invokeValueOf(final Class<T> token)
            throws NoSuchMethodError {
        try {
            final Method method = token.getMethod("valueOf", String.class);
            return value -> {
                try {
                    return (T) method.invoke(null, value);
                } catch (final IllegalAccessException e) {
                    final IllegalAccessError x = new IllegalAccessError(e.getMessage());
                    x.setStackTrace(e.getStackTrace());
                    throw x;
                } catch (final InvocationTargetException e) {
                    final Throwable root = getRootCause(e);
                    final RuntimeException x = new RuntimeException(root);
                    x.setStackTrace(root.getStackTrace());
                    throw x;
                }
            };
        } catch (final NoSuchMethodException ignored) {
            return null;
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static <T, E extends Exception> Conversion<T, E> invokeOf(final Class<T> token)
            throws NoSuchMethodError {
        try {
            final Method method = token.getMethod("of", String.class);
            return value -> {
                try {
                    return (T) method.invoke(null, value);
                } catch (final IllegalAccessException e) {
                    final IllegalAccessError x = new IllegalAccessError(e.getMessage());
                    x.setStackTrace(e.getStackTrace());
                    throw x;
                } catch (final InvocationTargetException e) {
                    final Throwable root = getRootCause(e);
                    final RuntimeException x = new RuntimeException(root);
                    x.setStackTrace(root.getStackTrace());
                    throw x;
                }
            };
        } catch (final NoSuchMethodException ignored) {
            return null;
        }
    }

    @Nullable
    private static <T, E extends Exception> Conversion<T, E> invokeConstructor(final Class<T> token)
            throws NoSuchMethodError {
        try {
            final Constructor<T> ctor = token.getConstructor(String.class);
            return value -> {
                try {
                    return ctor.newInstance(value);
                } catch (final IllegalAccessException e) {
                    final IllegalAccessError x = new IllegalAccessError(e.getMessage());
                    x.setStackTrace(e.getStackTrace());
                    throw x;
                } catch (final InvocationTargetException e) {
                    final Throwable root = getRootCause(e);
                    final RuntimeException x = new RuntimeException(root);
                    x.setStackTrace(root.getStackTrace());
                    throw x;
                } catch (final InstantiationException e) {
                    final InstantiationError x = new InstantiationError(e.getMessage());
                    x.setStackTrace(e.getStackTrace());
                    throw x;
                }
            };
        } catch (final NoSuchMethodException ignored) {
            return null;
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static <T> Class<T> tokenFor(final String type) {
        try {
            return (Class<T>) Class.forName(type);
        } catch (final ClassNotFoundException e) {
            final IllegalArgumentException x = new IllegalArgumentException(type + ": " + e);
            x.setStackTrace(e.getStackTrace());
            throw x;
        }
    }

    private final class FindValue
            extends StrLookup {
        @Override
        public String lookup(final String key) {
            final String value = getProperty(key);
            if (null != value)
                return value;
            final String sysprop = System.getProperty(key);
            if (null != sysprop)
                return sysprop;
            final String envvar = System.getenv(key);
            if (null != envvar)
                return envvar;
            throw new MissingPropertyException(key);
        }
    }

    /**
     * Converts a string property value into a typed object.
     *
     * @param <T> the converted type
     * @param <E> the exception type on failed converstion, {@code RuntimeException} if none
     */
    @FunctionalInterface
    public static interface Conversion<T, E extends Exception> {
        /**
         * Converts the given property <var>value</var> into a typed object.
         *
         * @param value the property value, never missing
         *
         * @return the typed object
         *
         * @throws E if conversion fails
         */
        T convert(@Nonnull final String value)
                throws E;
    }

    public static final class FailedConversionException extends RuntimeException {
        public FailedConversionException(final String key, final String value, final Throwable cause) {
            super(format("%s = %s", key, value), cause);
        }
    }

    public static class DuplicatePropertyException
            extends IllegalArgumentException {
        public DuplicatePropertyException(final String key) {
            super(key);
        }
    }

    public static class MissingPropertyException
            extends IllegalArgumentException {
        public MissingPropertyException(final String key) {
            super(key);
        }
    }

    private static final class Key {
        private final String property;
        private final Object fallback;

        private Key(final String property, final Object fallback) {
            this.property = property;
            this.fallback = fallback;
        }
    }

    private class Converted
            extends CacheLoader<Key, Object> {
        @Nullable
        @Override
        public Object load(@Nonnull final Key key)
                throws Exception {
            final String wholeValue = getProperty(key.property);
            if (null != wholeValue)
                return wholeValue; // Literal key match wins - assume T==String
            final String[] parts = colon.split(key.property, 2);
            final String value = getProperty(parts[1]);
            if (null == value)
                return key.fallback;
            return XProperties.factoryFor(parts[0]).convert(value);
        }
    }
}
