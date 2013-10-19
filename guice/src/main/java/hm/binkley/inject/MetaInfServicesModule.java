/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.kohsuke.MetaInfServices;

import static java.util.ServiceLoader.load;

/**
 * {@code MetaInfServicesModule} installs Guice modules annotated with {@link MetaInfServices} on
 * Guice {@link Module} via the JDK service loader.  This is a convenient way to bootstrap Guice
 * modules by dropping {@code META-INF/services/com.google.inject.Module} files into the classpath.
 * <p/>
 * Example: <pre>
 * &#64;MetaInfServices(Module.class)
 * public final class FooModule
 *         extends AbstractModule {
 *     &#64;Override
 *     protected void configure() {
 *         // Bindings here
 *     }
 * }</pre> At compile time the annotation processor for {@code MetaInfServces} writes "FooModule"
 * into {@code META-INF/services/com.google.inject.Module}.  At runtime this module finds {@code
 * FooModule} with {@code ServiceLoader.load(Module.class)} and installs in.
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
