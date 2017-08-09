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
import com.google.common.reflect.TypeToken;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.regex.Pattern;

import static com.google.common.primitives.Primitives.wrap;
import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;
import static java.net.InetSocketAddress.createUnresolved;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

/**
 * {@code Converter} is the opposite of {@code toString()}.  It turns strings
 * into objects.  Useful, for example, when obtaining Java value types from
 * XML.
 * <p>
 * Supports registered types <em>explicitly</em> with default {@link
 * #register(TypeToken, Conversion) registered} conversions: <ul><li>{@link
 * Class}</li>  <li>{@link InetAddress}</li> <li>{@link
 * InetSocketAddress}</li> <li>{@link Path}</li> <li>{@link Pattern}</li>
 * <li>{@link Resource}</li> <li>List of {@link Resource}s</li> <li>{@link
 * ResourceBundle}</li> <li>{@link TimeZone}</li> <li>{@link URI}</li></ul>
 * <p>
 * Supports other types <em>implicitly</em> by looking for a single-argument
 * string factory method or constructor in this order: <ol><li>Factory method
 * {@code parse(String)}</li> <li>Factory method {@code valueOf(String)}</li>
 * <li>Factory method {@code of(String)}</li> <li>Constructor {@code
 * T(String)}</li> <li>Constructor {@code T(CharSequence)}</li></ol>
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Remove duplication with xprop
 * @todo Discuss concurrency safety
 * @todo Do factory methods need to consider CharSequence?
 * @see #register(Class, Conversion)
 * @see #register(TypeToken, Conversion)
 */
public final class Converter {
    private static final MethodType STRING_CTOR = methodType(void.class,
            String.class);
    private static final MethodType CHAR_SEQUENCE_CTOR = methodType(
            void.class, CharSequence.class);
    private final Map<TypeToken<?>, Conversion<?, ? extends Exception>>
            conversions = new HashMap<>();

    {
        // JDK classes without standardly named String factory methods or String constructors
        register(Class.class, Class::forName);
        register(InetAddress.class, InetAddress::getByName);
        register(InetSocketAddress.class, value -> {
            final HostAndPort parsed = HostAndPort.fromString(value)
                    .requireBracketsForIPv6();
            return createUnresolved(parsed.getHost(), parsed.getPort());
        });
        register(Path.class, Paths::get);
        register(Pattern.class, Pattern::compile);
        register(Resource.class, value -> new DefaultResourceLoader(
                getClass().getClassLoader()).getResource(value));
        register(new TypeToken<List<Resource>>() {}, value -> asList(
                new PathMatchingResourcePatternResolver(
                        getClass().getClassLoader()).getResources(value)));
        register(ResourceBundle.class, ResourceBundle::getBundle);
        register(TimeZone.class, TimeZone::getTimeZone);
        register(URI.class, URI::create);
    }

    private static <T, E extends Exception> Conversion<T, E> parse(
            final TypeToken<T> type)
            throws NoSuchMethodError {
        return method(type, "parse");
    }

    private static <T, E extends Exception> Conversion<T, E> method(
            final TypeToken<T> type, final String name) {
        final Class<?> raw = type.getRawType();
        // TODO: Parameter type must match exactly
        try {
            return thunk(lookup().findStatic(raw, name,
                    methodType(raw, String.class)));
        } catch (final NoSuchMethodException | IllegalAccessException ignored) {
        }
        try {
            return thunk(lookup().findStatic(raw, name,
                    methodType(raw, CharSequence.class)));
        } catch (final NoSuchMethodException | IllegalAccessException ignored) {
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T, E extends Exception> Conversion<T, E> thunk(
            final MethodHandle handle) {
        return value -> {
            try {
                return (T) handle.invoke(value);
            } catch (final Error | RuntimeException e) {
                throw e;
            } catch (final Throwable t) {
                throw (E) t;
            }
        };
    }

    private static <T, E extends Exception> Conversion<T, E> valueOf(
            final TypeToken<T> type) {
        return method(type, "valueOf");
    }

    private static <T, E extends Exception> Conversion<T, E> of(
            final TypeToken<T> type) {
        return method(type, "of");
    }

    private static <T, E extends Exception> Conversion<T, E> ctor(
            final TypeToken<T> type) {
        final Class<?> raw = type.getRawType();
        // TODO: Parameter type must match exactly
        try {
            return thunk(lookup().findConstructor(raw, STRING_CTOR));
        } catch (final NoSuchMethodException | IllegalAccessException ignored) {
        }
        try {
            return thunk(lookup().findConstructor(raw, CHAR_SEQUENCE_CTOR));
        } catch (final NoSuchMethodException | IllegalAccessException ignored) {
        }
        return null;
    }

    /**
     * Registers an object conversion.  Use this for plain types without
     * consideration of generics.
     *
     * @param type the class token, never missing
     * @param factory the converter, never missing
     * @param <T> the conversion type
     *
     * @throws DuplicateConversion if the conversion is already registered
     */
    public <T> void register(@Nonnull final Class<T> type,
            @Nonnull final Conversion<T, ?> factory)
            throws DuplicateConversion {
        register(TypeToken.of(type), factory);
    }

    /**
     * Registers an object conversion.  Use this for types with generics, for
     * example, collections.
     *
     * @param type the Guava type token, never missing
     * @param factory the converter, never missing
     * @param <T> the conversion type
     *
     * @throws DuplicateConversion if the conversion is already registered
     */
    public <T> void register(@Nonnull final TypeToken<T> type,
            @Nonnull final Conversion<T, ?> factory)
            throws DuplicateConversion {
        if (null != conversions.putIfAbsent(type, factory))
            throw new DuplicateConversion(type);
    }

    /**
     * Registers a date format pattern for legacy {@code java.util.Date}.  By
     * default {@code Converter} uses the deprecated {@link Date#Date(String)}
     * constructor.
     * <p>
     * This method is a convenience and a caution for surprising legacy date
     * parsing.  Better is to use {@link java.time} classes.
     *
     * @param pattern the date format pattern, never missing
     */
    public void registerDate(@Nonnull final String pattern) {
        register(Date.class,
                value -> new SimpleDateFormat(pattern).parse(value));
    }

    /**
     * Converts the given <var>value</var> into an instance of
     * <var>type</var>.  Use this for plain types without consideraton of
     * generics.
     *
     * @param type the target type, never missing
     * @param value the string to convert, never missing
     * @param <T> the conversion type
     *
     * @return the converted instance of <var>type</var>, never missing
     *
     * @throws Exception if conversion fails
     */
    @Nonnull
    public <T> T convert(@Nonnull final Class<T> type,
            @Nonnull final String value)
            throws Exception {
        return convert(TypeToken.of(wrap(type)), value);
    }

    /**
     * Converts the given <var>value</var> into an instance of
     * <var>type</var>.  Use this for types with generis, for example,
     * collections.
     *
     * @param type the target type, never missing
     * @param value the string to convert, never missing
     * @param <T> the conversion type
     *
     * @return the converted instance of <var>type</var>, never missing
     *
     * @throws Exception if conversion fails
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    public <T> T convert(@Nonnull final TypeToken<T> type,
            @Nonnull final String value)
            throws Exception {
        final Class<? super T> rawType = type.getRawType();
        return String.class == rawType ? (T) value
                : factoryFor(type).convert(value);
    }

    /**
     * Get an unmodifiable view of registered conversions.  Supported
     * conversions are those implicit and those registered; registered takes
     * precedence.
     *
     * @return the set of registered conversion types, never missing
     *
     * @todo What is the best way to expose conversions for
     * inspection/management?
     */
    @Nonnull
    public Set<TypeToken<?>> registered() {
        return unmodifiableSet(conversions.keySet());
    }

    private <T, E extends Exception> Conversion<T, E> factoryFor(
            final TypeToken<T> type) {
        // Java 8 generics inference is brilliant but not perfect, requires the ugly cast
        return asList(
                (Function<TypeToken<T>, Conversion<T, E>>) this::getRegistered,
                Converter::parse, Converter::valueOf, Converter::of,
                Converter::ctor).stream().
                map(f -> f.apply(type)).
                filter(conversion -> null != conversion).
                findFirst().
                orElseThrow(() -> new UnsupportedConversion(type));
    }

    @SuppressWarnings("unchecked")
    private <T, E extends Exception> Conversion<T, E> getRegistered(
            final TypeToken<T> type) {
        return (Conversion<T, E>) conversions.get(type);
    }

    /**
     * Converts a string property value into a typed object.
     *
     * @param <T> the converted type
     * @param <E> the exception type on failed converstion, use {@code
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

    /** @todo Documentation */
    public static class DuplicateConversion
            extends IllegalArgumentException {
        private DuplicateConversion(final TypeToken<?> type) {
            super(type.toString());
        }
    }

    /** @todo Documentation */
    public static class UnsupportedConversion
            extends UnsupportedOperationException {
        private UnsupportedConversion(final TypeToken<?> type) {
            super(type.toString());
        }
    }
}
