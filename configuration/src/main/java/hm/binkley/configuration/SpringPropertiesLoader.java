package hm.binkley.configuration;

import com.google.common.base.Function;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Iterables.transform;
import static hm.binkley.configuration.Conversions.unchecked;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.reverse;

/**
 * {@code SpringPropertiesLoader} needs documentation.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public class SpringPropertiesLoader<E extends Exception>
        implements PropertiesLoader<E> {
    private final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    private final Function<Exception, E> exceptions;
    private final String locationPattern;

    public SpringPropertiesLoader(@Nonnull final Function<Exception, E> exceptions,
            @Nonnull final String locationPattern) {
        this.locationPattern = locationPattern;
        this.exceptions = exceptions;
    }

    public static SpringPropertiesLoader<RuntimeException> springPropertiesLoader(
            @Nonnull final String locationPattern) {
        return new SpringPropertiesLoader<>(unchecked(), locationPattern);
    }

    @Nonnull
    @Override
    public Properties load()
            throws E {
        try {
            final List<Resource> resources = new ArrayList<>(
                    asList(resolver.getResources(locationPattern)));
            reverse(resources);
            final Properties properties = new Properties();
            for (final Resource resource : resources)
                properties.load(resource.getInputStream());
            return properties;
        } catch (final IOException e) {
            throw exceptions.apply(e);
        }
    }

    @Nonnull
    @Override
    public String describe() {
        try {
            return on(", ").join(transform(asList(resolver.getResources(locationPattern)),
                    new Function<Resource, String>() {
                        @Nonnull
                        @Override
                        public String apply(final Resource resource) {
                            return resource.getDescription();
                        }
                    }));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    @Override
    public String toString() {
        return format("%s(%s)", getClass().getSimpleName(), locationPattern);
    }
}
