package hm.binkley.configuration;

import hm.binkley.configuration.EgConfiguration.EgConfigurationEnum;

import javax.annotation.Nonnull;

import static hm.binkley.configuration.EgConfiguration.EgConfigurationEnum.*;
import static java.lang.System.out;

/**
 * {@code EgConfiguration} needs documentation.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public class EgConfiguration
        implements
        Configuration<EgConfiguration, EgConfigurationEnum, Object, EgConfiguration.EgConfigurationException> {
    public static void main(final String... args)
            throws EgConfigurationException {
        final EgConfiguration configuration = new EgConfiguration();
        out.println(configuration.getFoo());
        out.println(configuration.getBar());
        out.println(configuration.getBaz());
    }

    public String getFoo()
            throws EgConfigurationException {
        return (String) lookup(FOO);
    }

    public int getBar()
            throws EgConfigurationException {
        return (int) lookup(BAR);
    }

    public Object getBaz()
            throws EgConfigurationException {
        return lookup(BAZ);
    }

    @Override
    public Object lookup(@Nonnull final EgConfigurationEnum key)
            throws EgConfigurationException {
        if (null == key.value)
            throw new EgConfigurationException(key);
        return key.value;
    }

    public enum EgConfigurationEnum {
        FOO("Sally"), BAR(4) {
            @Override
            public String toString() {
                return "No luck";
            }
        }, BAZ(null);
        private final Object value;

        private EgConfigurationEnum(final Object value) {
            this.value = value;
        }
    }

    public static class EgConfigurationException
            extends Exception {
        private EgConfigurationException(final EgConfigurationEnum e) {
            super(e.toString());
        }
    }
}
