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

import static java.util.Arrays.asList;
import static java.util.Collections.reverse;

/**
 * {@code SpringPropertiesLoader} needs documentation.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public final class SpringPropertiesLoader<E extends Exception>
        implements PropertiesLoader<E> {
    private final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    private final String locationPattern;
    private final Function<IOException, E> converter;

    public SpringPropertiesLoader(@Nonnull final String locationPattern,
            final Function<IOException, E> converter) {
        this.locationPattern = locationPattern;
        this.converter = converter;
    }

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
            throw converter.apply(e);
        }
    }
}
