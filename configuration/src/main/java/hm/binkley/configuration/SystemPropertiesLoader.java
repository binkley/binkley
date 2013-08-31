package hm.binkley.configuration;

import javax.annotation.Nonnull;
import java.util.Properties;

/**
 * {@code SystemPropertiesLoader} needs documentation.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public final class SystemPropertiesLoader<E extends Exception>
        implements PropertiesLoader<E> {
    private static final SystemPropertiesLoader LOADER = new SystemPropertiesLoader<>();

    @SuppressWarnings("unchecked")
    @Nonnull
    public static <E extends Exception> SystemPropertiesLoader<E> systemPropertiesLoader() {
        return LOADER;
    }

    @Nonnull
    @Override
    public Properties load() {
        return System.getProperties();
    }

    @Nonnull
    @Override
    public String describe() {
        return "system properties";
    }
}
