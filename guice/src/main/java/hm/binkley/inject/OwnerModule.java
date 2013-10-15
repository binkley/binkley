package hm.binkley.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * {@code OwnerModule} is light-weight wiring of an OWNER API config instance into Guice.
 * <p/>
 * Guice does not support generic provider methods; each config needs to be separately configured.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public final class OwnerModule<C extends Config>
        extends AbstractModule {
    private final Class<C> configType;
    private final Map<String, String> overrides = new HashMap<>();

    private OwnerModule(final Class<C> configType) {
        this.configType = configType;
    }

    private OwnerModule(final Class<C> configType, final Map<String, String> overrides) {
        this.configType = configType;
        this.overrides.putAll(overrides);
    }

    /**
     * Creates a new Guice module for the given OWNER API config class.
     *
     * @param configType the config class, never missing
     * @param <C> the config type
     *
     * @return the Guice module, never missing
     */
    @Nonnull
    public static <C extends Config> Module ownerModule(@Nonnull final Class<C> configType) {
        return new OwnerModule<>(configType);
    }

    /**
     * Creates a new Guice module for the given OWNER API config class with <var>overrides</var>,
     * for example from commandline options.
     *
     * @param configType the config class, never missing
     * @param overrides more configuration, never missing
     * @param <C> the config type
     *
     * @return the Guice module, never missing
     */
    @Nonnull
    public static <C extends Config> Module ownerModule(@Nonnull final Class<C> configType,
            @Nonnull final Map<String, String> overrides) {
        return new OwnerModule<>(configType, overrides);
    }

    @Override
    protected void configure() {
        bind(configType).toInstance(ConfigFactory
                .create(configType, overrides, System.getProperties(), System.getenv()));
    }
}
