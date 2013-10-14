package hm.binkley.inject;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Test;

import javax.inject.Inject;

/**
 * {@code OwnerModuleTest} tests {@link OwnerModule}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public class OwnerModuleTest {
    @Test
    public void should() {
        final Injector guice = Guice.createInjector(new OwnerModule());
    }


    public static final class Bob {
        @Inject
        public Bob() {

        }
    }
}
