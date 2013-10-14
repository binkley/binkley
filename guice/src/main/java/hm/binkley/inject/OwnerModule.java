package hm.binkley.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;

import javax.annotation.Nonnull;

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

    private OwnerModule(final Class<C> configType) {
        this.configType = configType;
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

    @Override
    protected void configure() {
        bind(configType).toInstance(
                ConfigFactory.create(configType, System.getProperties(), System.getenv()));
    }
}
