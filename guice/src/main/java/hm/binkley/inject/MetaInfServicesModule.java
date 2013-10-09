package hm.binkley.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.kohsuke.MetaInfServices;

import static java.util.ServiceLoader.load;

/**
 * {@code MetaInfServicesModule} installs Guice modules annotated with {@link MetaInfServices} on
 * {@link Module}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public final class MetaInfServicesModule
        extends AbstractModule {
    @Override
    protected void configure() {
        for (final Module module : load(Module.class))
            install(module);
    }
}
