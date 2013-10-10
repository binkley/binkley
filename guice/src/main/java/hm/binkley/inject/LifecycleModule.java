package hm.binkley.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.kohsuke.MetaInfServices;
import org.nnsoft.guice.lifegycle.AfterInjectionModule;
import org.nnsoft.guice.lifegycle.DisposeModule;
import org.nnsoft.guice.lifegycle.Disposer;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import static com.google.inject.matcher.Matchers.any;
import static java.lang.Runtime.getRuntime;

/**
 * {@code LifecycleModule} enables {@link PostConstruct} and {@link PreDestroy} in Guice.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
@MetaInfServices(Module.class)
public final class LifecycleModule
        extends AbstractModule {
    /**
     * After creating the Guice injector, invoke this method on the injector to enable {@link
     * PreDestroy} when the JVM shuts down.
     *
     * @param guice the Guice injector, never missing
     */
    public static void enablePreDestroy(@Nonnull final Injector guice) {
        getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                guice.getInstance(Disposer.class).dispose();
            }
        });
    }

    @Override
    protected void configure() {
        install(new AfterInjectionModule(PostConstruct.class, any()));
        install(new DisposeModule(PreDestroy.class, any()));
    }
}
