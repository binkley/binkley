package hm.binkley.configuration;

import hm.binkley.configuration.EgConfigurationEnum.EgException;

import javax.annotation.Nonnull;

import static java.lang.System.out;

/**
 * {@code EgConfigurationEnum} is an example of {@code enum} configuration keys.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public enum EgConfigurationEnum
        implements Configuration<EgConfigurationEnum, Object, String, EgException> {
    FOO() {
        @Override
        public String lookup(@Nonnull final Object key)
                throws EgException {
            return "Fetched from somewhere: " + key;
        }
    },
    BAR {
        @Override
        public String lookup(@Nonnull final Object key)
                throws EgException {
            throw new EgException(this, key);
        }

        @Override
        public String toString() {
            return "Failed somewhere else";
        }
    };

    public static void main(final String... args) {
        try {
            out.println(FOO.lookup("bob"));
            out.println(BAR.lookup("bob"));
        } catch (final EgException e) {
            e.printStackTrace();
        }
    }

    public static final class EgException
            extends Exception {
        private EgException(final EgConfigurationEnum e, final Object key) {
            super(e + ": " + key);
        }
    }
}
