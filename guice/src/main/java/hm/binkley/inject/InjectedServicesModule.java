/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.kohsuke.MetaInfServices;

import javax.annotation.Nonnull;

import static java.util.ServiceLoader.load;

/**
 * {@code InjectedServicesModule} installs injected Guice modules annotated with {@link
 * MetaInfServices} on Guice {@link Module} via the JDK service loader.  This is a convenient way to
 * bootstrap Guice modules by dropping {@code META-INF/services/com.google.inject.Module} files into
 * the classpath.
 *
 * Example: <pre>
 * &#64;MetaInfServices(Module.class)
 * public final class FooModule
 *         extends AbstractModule {
 *     private final String something;
 *
 *     &#64;Inject
 *     public FooModule(&#64;Named("some-property") &#64;Nonnull final String something) {
 *         this.something = something;
 *     }
 *
 *     &#64;Override
 *     protected void configure() {
 *         // Bindings here - use "something" field
 *     }
 * }</pre> At compile time the annotation processor for {@code MetaInfServces} writes "FooModule"
 * into {@code META-INF/services/com.google.inject.Module}.  At runtime this module finds {@code
 * FooModule} with {@code ServiceLoader.load(Module.class)}, injects it with the parent, and
 * installs in.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public final class InjectedServicesModule
        extends AbstractModule {
    private final Injector parent;

    public InjectedServicesModule(@Nonnull final Injector parent) {
        this.parent = parent;
    }

    @Override
    protected void configure() {
        for (final Module module : load(Module.class)) {
            parent.injectMembers(module);
            install(module);
        }
    }
}
