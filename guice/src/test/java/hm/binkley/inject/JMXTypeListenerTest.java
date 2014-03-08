/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.inject;

import com.google.inject.AbstractModule;
import com.google.inject.ProvisionException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.softee.management.annotation.MBean;

import static com.google.inject.Guice.createInjector;
import static hm.binkley.inject.JMXTypeListener.bindJMX;
import static org.hamcrest.Matchers.containsString;

/**
 * {@code JMXTypeListenerTest} tests {@link JMXTypeListener}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public class JMXTypeListenerTest {
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldThrowCorrectly() {
        thrown.expect(ProvisionException.class);
        thrown.expectMessage(containsString("Key properties cannot be empty"));

        createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bindJMX(binder());
            }
        }).getInstance(BadMBean.class);
    }

    @MBean(objectName = "xxx!!!")
    public static final class BadMBean {}
}
