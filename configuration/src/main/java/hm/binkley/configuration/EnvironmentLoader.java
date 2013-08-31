package hm.binkley.configuration;

import javax.annotation.Nonnull;
import java.util.Properties;

import static java.lang.System.getenv;

/**
 * {@code EnvironmentLoader} needs documentation.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public final class EnvironmentLoader<E extends Exception>
        implements PropertiesLoader<E> {
    private static final EnvironmentLoader LOADER = new EnvironmentLoader<>();

    @SuppressWarnings("unchecked")
    @Nonnull
    public static <E extends Exception> EnvironmentLoader<E> environmentLoader() {
        return LOADER;
    }

    @Nonnull
    @Override
    public Properties load() {
        final Properties properties = new Properties();
        properties.putAll(getenv());
        return properties;
    }

    @Nonnull
    @Override
    public String describe() {
        return "environment";
    }

    @Nonnull
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
