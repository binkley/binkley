package hm.binkley.configuration;

import hm.binkley.configuration.SpringFormatConfiguration.DefaultSpringFormatConfiguration;

import static java.lang.System.out;

/**
 * {@code EgSpringConfiguration} needs documentation.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public class EgSpringConfiguration
        extends DefaultSpringFormatConfiguration {
    public EgSpringConfiguration() {
        super("hm/binkley/configuration/**/eg*.properties", "eg.%s");
    }

    public static void main(final String... args) {
        final EgSpringConfiguration configuration = new EgSpringConfiguration();
        out.println(configuration.getFoo());
        out.println(configuration.getBar());
        out.println(configuration.getBaz());
    }

    public String getFoo() {
        return lookup("foo");
    }

    public int getBar() {
        return Integer.valueOf(lookup("bar"));
    }

    public Object getBaz() {
        return lookup("baz");
    }
}
