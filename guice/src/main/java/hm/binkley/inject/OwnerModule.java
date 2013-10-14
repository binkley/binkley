package hm.binkley.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;

import javax.annotation.Nonnull;

/**
 * {@code OwnerModule} needs documentation.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public final class OwnerModule<C extends Config>
        extends AbstractModule {
    private final Class<C> configType;

    private OwnerModule(final Class<C> configType) {
        this.configType = configType;
    }

    public static <C extends Config> Module ownerModule(@Nonnull final Class<C> configType) {
        return new OwnerModule<>(configType);
    }

    @Override
    protected void configure() {
        bind(configType).toInstance(
                ConfigFactory.create(configType, System.getProperties(), System.getenv()));
    }
}
