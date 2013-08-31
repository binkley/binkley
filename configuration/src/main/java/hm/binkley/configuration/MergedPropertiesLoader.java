package hm.binkley.configuration;

import com.google.common.base.Function;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Iterables.transform;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.reverse;

/**
 * {@code MergedPropertiesLoader} needs documentation.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public final class MergedPropertiesLoader<E extends Exception>
        implements PropertiesLoader<E> {
    private final List<PropertiesLoader<E>> loaders;

    private MergedPropertiesLoader(@Nonnull final List<PropertiesLoader<E>> loaders) {
        this.loaders = new ArrayList<>(loaders);
        reverse(this.loaders);
    }

    @Nonnull
    public static <E extends Exception> MergedPropertiesLoader<E> merge(
            final List<PropertiesLoader<E>> loaders) {
        return new MergedPropertiesLoader<>(loaders);
    }

    @Nonnull
    @SafeVarargs
    public static <E extends Exception> MergedPropertiesLoader<E> merge(
            final PropertiesLoader<E>... loaders) {
        return merge(asList(loaders));
    }

    @Nonnull
    @Override
    public Properties load()
            throws E {
        final Properties properties = new Properties();
        for (final PropertiesLoader<E> loader : loaders)
            properties.putAll(loader.load());
        return properties;
    }

    @Nonnull
    @Override
    public String describe() {
        return on(", ").join(transform(loaders, new Function<PropertiesLoader<E>, String>() {
            @Nonnull
            @Override
            public String apply(final PropertiesLoader<E> loader) {
                return loader.describe();
            }
        }));
    }

    @Nonnull
    @Override
    public String toString() {
        return format("%s%s", getClass().getSimpleName(), loaders);
    }
}
