package lab.dynafig.spring;

import lab.dynafig.Tracking;
import lombok.ToString;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.System.out;
import static org.springframework.boot.SpringApplication.run;

/**
 * {@code DynafigSpringMain} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">B. K. Oxley</a>
 * @todo Needs documentation
 */
@EnableAutoConfiguration
public class DynafigSpringMain {
    public static void main(final String... args) {
        out.println(run(DynafigSpringMain.class, args).
                getBean(Foo.class));
    }

    @Component
    @ToString
    public static class Foo {
        private final AtomicReference<String> x;

        @Inject
        public Foo(final Tracking dynafig) {
            x = dynafig.track("foo").get();
        }
    }
}
