/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util;

import hm.binkley.util.XPropsConverter.Conversion;
import lombok.NonNull;
import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static java.util.regex.Pattern.compile;

/**
 * {@code XProperties} is a {@code java.util.Properties} supporting inclusion of other properties
 * files, parameter substition and typed return values.  Key order is preserved; keys from included
 * properties are prior to those from the current properties.
 * <p>
 * Keys are restricted to {@code String}s.
 * <p>
 * Loading processes lines of the form: <pre>
 * #include <var>Spring style resource path</var></pre> for inclusion.  Looks up <var>resource
 * path</var> with a {@link ResourcePatternResolver}; loading found resources in turn. Similarly
 * processes resource recursively for further inclusion.  Includes multiple resources separated by
 * comma (",") in the same "#include" statement.
 * <p>
 * Substitutes in resource paths of the
 * form: <pre>
 * #include ${<var>variable</var>}</pre> or portions thereof.  Looks up <var>variable</var> in
 * the current properties, and if not found, in the system properties.  If found, replaces the text
 * with the variable value, including "${" and trailing "}".
 * <p>
 * Use forward slashes ("/") only for path separators; <strong>do not</strong> use back slashes
 * ("\").
 * <p>
 * Examples: <table><tr><th>Text</th> <th>Result</th></tr> <tr><td>{@code #include
 * foo/file.properties}</td> <td>Includes {@code foo.properties} found in the classpath</td></tr>
 * <tr><td>{@code #include foo.properties, bar.properties}</td> <td>Includes both {@code
 * foo.properties} and {@code bar.properties}</td></tr> <tr><td>{@code #include
 * file:/var/tmp/${user.name }/foo.properties}</td> <td>Includes {@code foo.properties} found in a
 * directory named after the current user</td></tr> <tr><td>{@code #include
 * classpath*:**&#47;foo.properties}</td> <td>Includes all {@code foo.properties}</tr> files found
 * subdirectories of the classpath</td></tr> </table>
 *
 * @todo Implement defaults
 * @todo Converter assumes cacheable keys; is this correct?
 * @see PathMatchingResourcePatternResolver
 * @see StrSubstitutor
 * @see XPropsConverter
 * @see #load(Reader) loading properties with inclusions
 * @see #load(InputStream) loading properties with inclusions
 * @see #getProperty(String) getting properties with substitution
 */
public class XProperties
        extends OrderedProperties {
    private static final Pattern include = compile("^#include\\s+(.*)\\s*$");
    private static final Pattern comma = compile("\\s*,\\s*");
    private static final Pattern colon = compile(":");

    private final Set<URI> included = new LinkedHashSet<>();
    private final XPropsConverter converter = new XPropsConverter();
    private final Map<Key, Object> converted = new ConcurrentHashMap<>();

    private final StrSubstitutor substitutor = new StrSubstitutor(new FindValue());

    {
        substitutor.setEnableSubstitutionInVariables(true);
    }

    /**
     * Creates a new {@code XProperties} for the given <var>absolutePath</var> found in the
     * classpath.
     *
     * @param absolutePath the absolute path to search on the classpath, never missing
     *
     * @throws IOException if <var>absolutePath</var> cannot be loaded
     */
    @Nonnull
    public static XProperties from(@Nonnull @NonNull final String absolutePath)
            throws IOException {
        final URL resource = XProperties.class.getResource(absolutePath);
        try (final InputStream in = resource.openStream()) {
            final XProperties xprops = new XProperties();
            xprops.included.add(URI.create(resource.toString()));
            xprops.load(in);
            return xprops;
        }
    }

    /** @todo Documentation */
    public XProperties() {
    }

    /** @todo Documentation */
    public XProperties(@Nonnull final Map<String, String> initial) {
        putAll(initial);
    }

    /**
     * @param prefix the alias prefix, never missing
     * @param factory the factory, never missing
     *
     * @todo Documentation
     * @see XPropsConverter#register(String, Conversion)
     */
    public void register(@Nonnull final String prefix,
            @Nonnull final Conversion<?, ? extends Exception> factory) {
        converter.register(prefix, factory);
    }

    /**
     * Note {@code XProperties} description for additional features over plain properties loading.
     * {@inheritDoc}
     *
     * @throws IOException if the properties cannot be loaded or if included resources cannot be
     * read
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
                            for (final Resource resource : loader.getResources(x)) {
                                final URI uri = resource.getURI();
                                if (!included.add(uri))
                                    throw new RecursiveIncludeException(uri, included);
                                try (final InputStream in = resource.getInputStream()) {
                                    load(in);
                                }
                            }
                }
            }

            super.load(new CharArrayReader(writer.toCharArray()));
        }
        included.clear();
    }

    /**
     * Note {@code XProperties} description for additional features over plain properties loading.
     * {@inheritDoc}
     *
     * @throws IOException if the properties cannot be loaded or if included resources cannot be
     * read
     */
    @Override
    public synchronized void load(final InputStream inStream)
            throws IOException {
        load(new InputStreamReader(inStream, Charset.forName("UTF-8")));
    }

    /**
     * {@inheritDoc} <p/>
     * Substitutes in property values sequences of the form: <pre>
     * ${<var>variable</var>}</pre>.  Looks up <var>variable</var> in the current properties, and
     * if not found, in the system properties.  If found, replaces the text with the variable value,
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
     * Typed keys are of the form: {@code <var>type</var>:<var>key</var>}.  The <var>key</var> is
     * the same key as {@link System#getProperty(String) System.getProperty}.  See {@link
     * XPropsConverter#register(String, Conversion) register} for built-in <var>type</var> key
     * prefixes.
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
     * @see XPropsConverter#register(String, Conversion)
     */
    @Nullable
    public <T> T getObject(@Nonnull final String key) {
        return getObjectOrDefault(key, null);
    }

    /** @todo Javadoc */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getObjectOrDefault(@Nonnull final String key, final T defaultValue) {
        return (T) converted.computeIfAbsent(new Key(key, defaultValue), this::convert);
    }

    private Object convert(final Key key) {
        final String property = key.property;
        final String wholeValue = getProperty(property);
        if (null != wholeValue)
            return wholeValue; // Literal key match wins - assume T==String
        final String[] parts = colon.split(property, 2);
        final String value = getProperty(parts[1]);
        if (null == value)
            return key.fallback;
        try {
            return converter.convert(parts[0], value);
        } catch (final Exception e) {
            final String[] x = colon.split(property, 2);
            throw new FailedConversionException(property, getProperty(x[1]), e.getCause());
        }
    }

    private final class FindValue
            extends StrLookup {
        @Override
        public String lookup(final String key) {
            return asList((Function<String, String>) XProperties.this::getProperty,
                    System::getProperty, System::getenv).stream().
                    map(f -> f.apply(key)).
                    filter(v -> null != v).
                    findFirst().
                    orElseThrow(() -> new MissingPropertyException(key));
        }
    }

    /** @todo Documentation */
    public static final class RecursiveIncludeException
            extends RuntimeException {
        public RecursiveIncludeException(final URI duplicate, final Collection<URI> included) {
            super(message(duplicate, included));
        }

        private static String message(final URI duplicate, final Collection<URI> included) {
            final ArrayList<URI> x = new ArrayList<>(included.size() + 1);
            x.addAll(included);
            x.add(duplicate);
            return x.toString();
        }
    }

    /** @todo Documentation */
    public static final class FailedConversionException
            extends RuntimeException {
        public FailedConversionException(final String key, final String value,
                final Throwable cause) {
            super(format("%s = %s", key, value), cause);
        }
    }

    /** @todo Documentation */
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
}
