/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.inject;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import org.softee.management.annotation.MBean;
import org.softee.management.exception.ManagementException;
import org.softee.management.helper.MBeanRegistration;

import javax.annotation.Nonnull;
import javax.management.MalformedObjectNameException;

import static com.google.common.base.Throwables.getRootCause;

/**
 * {@code JMXTypeListener} registers JMX mbeans using pojo-mbean.
 *
 * MBean registration errors are rethrown as {@code RuntimeException} to get Guice to correctly
 * handle them as provision exceptions.  The message and call stack us updated to reflect the actual
 * exception.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public final class JMXTypeListener
        implements TypeListener {
    private static final MBeanMatcher MATCHER = new MBeanMatcher();
    private static final JMXTypeListener LISTENER = new JMXTypeListener();

    /**
     * Binds a {@code JMXTypeListener} to the given <var>binder</var>, matching on types annotated
     * with pojo-mbean {@link MBean}.
     *
     * @param binder the Guice binder, never missing
     */
    public static void bindJMX(@Nonnull final Binder binder) {
        binder.bindListener(MATCHER, LISTENER);
    }

    @Override
    public <I> void hear(final TypeLiteral<I> type, final TypeEncounter<I> encounter) {
        encounter.register((InjectionListener<I>) injectee -> {
            try {
                new MBeanRegistration(injectee).register();
            } catch (final MalformedObjectNameException | ManagementException e) {
                // Guice will handle RuntimeException during provision.  Update the stack trace
                // to bring the root cause to the fore
                final Throwable cause = getRootCause(e);
                final RuntimeException x = new RuntimeException(cause.toString());
                x.setStackTrace(cause.getStackTrace());
                throw x;
            }
        });
    }

    public static class MBeanMatcher
            extends AbstractMatcher<TypeLiteral<?>> {
        @Override
        public boolean matches(final TypeLiteral<?> literal) {
            return literal.getRawType().isAnnotationPresent(MBean.class);
        }
    }
}
