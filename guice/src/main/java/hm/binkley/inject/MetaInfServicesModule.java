package hm.binkley.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.kohsuke.MetaInfServices;

import static java.util.ServiceLoader.load;

/**
 * {@code MetaInfServicesModule} installs Guice modules annotated with {@link MetaInfServices} on
 * Guice {@link Module} via the JDK service loader.  This is a convenient way to bootstrap Guice
 * modules by dropping {@code META-INF/services/com.google.inject.Module} files into the classpath.
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
