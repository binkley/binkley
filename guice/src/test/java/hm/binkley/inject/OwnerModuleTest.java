package hm.binkley.inject;

import org.aeonbits.owner.Config;
import org.junit.Test;

import javax.inject.Inject;

import static com.google.inject.Guice.createInjector;
import static hm.binkley.inject.OwnerModule.ownerModule;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@code OwnerModuleTest} tests {@link OwnerModule}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public class OwnerModuleTest {
    @Test
    public void should() {
        assertThat(createInjector(ownerModule(BobConfig.class)).getInstance(Bob.class).config.foo(),
                is(equalTo(3)));
    }

    public interface BobConfig
            extends Config {
        @DefaultValue("3")
        int foo();
    }

    public static final class Bob {
        private final BobConfig config;

        @Inject
        public Bob(final BobConfig config) {
            this.config = config;
        }
    }
}
