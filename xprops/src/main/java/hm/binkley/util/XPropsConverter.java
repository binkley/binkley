/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org/>.
 */

package hm.binkley.util;

import com.google.common.net.HostAndPort;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.annotation.Nonnull;
import java.io.File;
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
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static com.google.common.base.Throwables.getRootCause;
import static java.lang.Thread.currentThread;
import static java.net.InetSocketAddress.createUnresolved;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

/**
 * {@code XPropsConverter} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 * @todo Replace with prefix->class mapping and use the other Converter
 */
public final class XPropsConverter {
    private final Map<String, Conversion<?, ? extends Exception>> factories
            = new ConcurrentHashMap<>();

    public XPropsConverter() {
        register("address", value -> {
            final HostAndPort parsed = HostAndPort.fromString(value)
                    .requireBracketsForIPv6();
            return createUnresolved(parsed.getHostText(), parsed.getPort());
        });
        register("bundle", ResourceBundle::getBundle);
        register("byte", Byte::valueOf);
        register("class", Class::forName);
        register("date", LocalDate::parse);
        register("decimal", BigDecimal::new);
        register("double", Double::valueOf);
        register("duration", Duration::parse);
        register("file", File::new);
        register("float", Float::valueOf);
        register("inet", InetAddress::getByName);
        register("int", Integer::valueOf);
        register("integer", BigInteger::new);
        register("long", Long::valueOf);
        register("path", Paths::get);
        register("period", Period::parse);
        register("regex", Pattern::compile);
        register("resource", value -> new DefaultResourceLoader(
                currentThread().getContextClassLoader()).getResource(value));
        register("resource*", value -> asList(
                new PathMatchingResourcePatternResolver(
                        currentThread().getContextClassLoader())
                        .getResources(value)));
        register("short", Short::valueOf);
        register("time", LocalDateTime::parse);
        register("timestamp", Instant::parse);
        register("tz", TimeZone::getTimeZone);
        register("uri", URI::create);
        register("url", URL::new);
    }

    /**
     * Gets the unmodifiable set of conversion keys.
     *
     * @return the conversion keys, never missing
     */
    @Nonnull
    public Set<String> keys() {
        return unmodifiableSet(factories.keySet());
    }

    /**
     * Registers a new alias mapping for converting property values to typed
     * objects using the given <var>prefix</var> and <var>factory</var>.
     * Factories <strong>should</strong> never return {@code null} values
     * <p>
     * The <var>type</var> prefix represents a Java or JDK type as listed here:
     * <table><tr><th>Prefix</th> <th>Type</th></tr> <tr><td>address</td>
     * <td>{@code java.net.InetSocketAddress}</td></tr> <tr><td>bundle</td>
     * <td>{@code java.util.ResourceBundle}</td></tr> <tr><td>byte</td>
     * <td>{@code java.lang.Byte}</td></tr> <tr><td>class</td>
     * <td>java.lang.Class</td></tr> <tr><td>date</td> <td>{@code
     * java.time.LocalDate}</td></tr> <tr><td>decimal</td> <td>{@code java.math
     * .BigDecimal}</td></tr> <tr><td>double</td> <td>{@code
     * java.lang.Double}</td></tr> <tr><td>duration</td> <td>{@code
     * java.time.Duration}</td></tr> <tr><td>file</td> <td>{@code
     * java.io.File}</td></tr> <tr><td>float</td> <td>{@code
     * java.lang.Float}</td></tr> <tr><td>inet</td> <td>{@code
     * java.net.InetAddress}</td></tr> <tr><td>int</td> <td>{@code
     * java.lang.Integer}</td></tr> <tr><td>integer</td> <td>{@code
     * java.math.BigInteger}</td></tr> <tr><td>long</td> <td>{@code
     * java.lang.Long}</td></tr> <tr><td>path</td> <td>{@code
     * java.nio.file.Path}</td></tr> <tr><td>period</td> <td>{@code
     * java.time.Period}</td></tr> <tr><td>resource</td> <tr><td>regex</td>
     * <td>{@code java.util.regex.Pattern}</td></tr> <td>{@code
     * org.springframework.core.io.Resource}</td></tr> <tr><td>resource*</td>
     * <td>{@code java.util.List&lt;org.springframework.core.io.Resource&gt;
     * }</td></tr> <tr><td>short</td> <td>{@code java.lang.Short}</td></tr>
     * <tr><td>time</td> <td>{@code java.time.LocalDateTime}</td></tr>
     * <tr><td>timestamp</td> <td>{@code java.time.Instant}</td></tr>
     * <tr><td>tz</td> <td>{@code java.util.TimeZone}</td></tr> <tr><td>uri</td>
     * <td>{@code java.net.URI}</td></tr> <tr><td>url</td> <td>{@code
     * java.net.URL}</td></tr> </table>
     *
     * @param prefix the alias prefix, never missing
     * @param factory the factory, never missing
     *
     * @throws DuplicateConversionException if <var>prefix</var> is already
     * registered
     */
    public void register(@Nonnull final String prefix,
            @Nonnull final Conversion<?, ?> factory)
            throws DuplicateConversionException {
        if (null != factories.putIfAbsent(prefix, factory))
            throw new DuplicateConversionException(prefix);
    }

    /**
     * Converts the given <var>value</var> by <var>key</var>.
     *
     * @param key the conversion key, never missing
     * @param value the value
     * @param <T> the converted type, never missing
     *
     * @return the converted value
     *
     * @throws Exception if conversion fails
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    public <T> T convert(@Nonnull final String key, @Nonnull final String value)
            throws Exception {
        return (T) factoryFor(key).convert(value);
    }

    @Nonnull
    @SuppressWarnings({"unchecked", "ReuseOfLocalVariable"})
    private <T, E extends Exception> Conversion<T, E> factoryFor(
            final String type) {
        // Look for alias first
        Conversion<T, E> factory = (Conversion<T, E>) factories.get(type);
        if (null != factory)
            return factory;
        final Class<T> token = tokenFor(type);
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

    @SuppressWarnings("unchecked")
    private static <T, E extends Exception> Conversion<T, E> invokeValueOf(
            final Class<T> token)
            throws NoSuchMethodError {
        try {
            final Method method = token.getMethod("valueOf", String.class);
            return value -> {
                try {
                    return (T) method.invoke(null, value);
                } catch (final IllegalAccessException e) {
                    final IllegalAccessError x = new IllegalAccessError(
                            e.getMessage());
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

    @SuppressWarnings("unchecked")
    private static <T, E extends Exception> Conversion<T, E> invokeOf(
            final Class<T> token)
            throws NoSuchMethodError {
        try {
            final Method method = token.getMethod("of", String.class);
            return value -> {
                try {
                    return (T) method.invoke(null, value);
                } catch (final IllegalAccessException e) {
                    final IllegalAccessError x = new IllegalAccessError(
                            e.getMessage());
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

    private static <T, E extends Exception> Conversion<T, E> invokeConstructor(
            final Class<T> token)
            throws NoSuchMethodError {
        try {
            final Constructor<T> ctor = token.getConstructor(String.class);
            return value -> {
                try {
                    return ctor.newInstance(value);
                } catch (final IllegalAccessException e) {
                    final IllegalAccessError x = new IllegalAccessError(
                            e.getMessage());
                    x.setStackTrace(e.getStackTrace());
                    throw x;
                } catch (final InvocationTargetException e) {
                    final Throwable root = getRootCause(e);
                    final RuntimeException x = new RuntimeException(root);
                    x.setStackTrace(root.getStackTrace());
                    throw x;
                } catch (final InstantiationException e) {
                    final InstantiationError x = new InstantiationError(
                            e.getMessage());
                    x.setStackTrace(e.getStackTrace());
                    throw x;
                }
            };
        } catch (final NoSuchMethodException ignored) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> tokenFor(final String type) {
        try {
            return (Class<T>) Class.forName(type);
        } catch (final ClassNotFoundException e) {
            final IllegalArgumentException x = new IllegalArgumentException(
                    type + ": " + e);
            x.setStackTrace(e.getStackTrace());
            throw x;
        }
    }

    /**
     * Converts a string property value into a typed object.
     *
     * @param <T> the converted type
     * @param <E> the exception type on failed converstion, {@code
     * RuntimeException} if none
     */
    @FunctionalInterface
    public interface Conversion<T, E extends Exception> {
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

    /** Duplicate key registered with {@link XPropsConverter}. */
    public static class DuplicateConversionException
            extends IllegalArgumentException {
        /**
         * Constructs a new {@code DuplicateConversionException} for the given
         * <var>key</var>.
         *
         * @param key the conversion key, never missing
         */
        public DuplicateConversionException(@Nonnull final String key) {
            super(key);
        }
    }
}
