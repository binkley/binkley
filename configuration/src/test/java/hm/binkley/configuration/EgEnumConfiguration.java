package hm.binkley.configuration;

import hm.binkley.configuration.EgEnumConfiguration.EgEnum;
import hm.binkley.configuration.EgEnumConfiguration.EgException;

import javax.annotation.Nonnull;

import static hm.binkley.configuration.EgEnumConfiguration.EgEnum.*;
import static java.lang.System.out;

/**
 * {@code EgEnumConfiguration} needs documentation.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public class EgEnumConfiguration
        implements Configuration<EgEnumConfiguration, EgEnum, Object, EgException> {
    public static void main(final String... args)
            throws EgException {
        final EgEnumConfiguration configuration = new EgEnumConfiguration();
        out.println(configuration.getFoo());
        out.println(configuration.getBar());
        out.println(configuration.getBaz());
    }

    public String getFoo()
            throws EgException {
        return (String) lookup(FOO);
    }

    public int getBar()
            throws EgException {
        return (int) lookup(BAR);
    }

    public Object getBaz()
            throws EgException {
        return lookup(BAZ);
    }

    @Override
    public Object lookup(@Nonnull final EgEnum key)
            throws EgException {
        if (null == key.value)
            throw new EgException(key);
        return key.value;
    }

    public enum EgEnum {
        FOO("Sally"), BAR(4) {
            @Override
            public String toString() {
                return "No luck";
            }
        }, BAZ(null);
        private final Object value;

        private EgEnum(final Object value) {
            this.value = value;
        }
    }

    public static class EgException
            extends Exception {
        private EgException(final EgEnum e) {
            super(e.toString());
        }
    }
}
