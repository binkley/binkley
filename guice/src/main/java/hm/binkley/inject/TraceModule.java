/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.matcher.AbstractMatcher;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.kohsuke.MetaInfServices;
import org.slf4j.ext.XLogger;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static com.google.inject.matcher.Matchers.any;
import static org.slf4j.ext.XLoggerFactory.getXLogger;

/**
 * {@code TraceModule} is a Guice module to enable tracing via SLF4J of method entry/exit and
 * exceptions thrown (exception {@code Error}s).  Use {@link ServicesModule} to automatically
 * enable. Use {@link Trace @Trace} to mark classes or individual methods for tracing. Use <a
 * href="http://docs.oracle.com/javase/7/docs/technotes/guides/language/assert.html#enable-disable">
 * {@code -ea}</a> to enable/disable at runtime.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
@MetaInfServices(Module.class)
public final class TraceModule
        extends AbstractModule {
    @Override
    protected void configure() {
        final ShouldTrace matcher = new ShouldTrace();
        final Tracer interceptor = new Tracer();
        bindInterceptor(matcher, any(), interceptor);
        bindInterceptor(any(), matcher, interceptor);
    }

    private static final class Tracer
            implements MethodInterceptor {
        private static XLogger logger(final MethodInvocation invocation)
                throws IllegalAccessException {
            final Class<?> declaring = invocation.getMethod().getDeclaringClass();
            for (final Field field : declaring.getDeclaredFields())
                if (XLogger.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    return XLogger.class.cast(field.get(invocation.getThis()));
                }
            return getXLogger(declaring);
        }

        @Override
        public Object invoke(final MethodInvocation invocation)
                throws Throwable {
            final XLogger logger = logger(invocation);
            logger.entry(invocation.getArguments());
            try {
                final Object proceed = invocation.proceed();
                if (void.class == invocation.getMethod().getReturnType())
                    logger.exit();
                else
                    logger.exit(proceed);
                return proceed;
            } catch (final Error e) {
                // Pass Error straight through, no logging - state of VM unknown
                throw e;
            } catch (final Throwable e) {
                logger.throwing(e);
                throw e;
            }
        }
    }

    private static final class ShouldTrace
            extends AbstractMatcher<AnnotatedElement> {
        @Override
        public boolean matches(final AnnotatedElement element) {
            final Class<?> type;
            if (element instanceof Method)
                type = Method.class.cast(element).getDeclaringClass();
            else if (element instanceof Class)
                type = Class.class.cast(element);
            else
                return false;

            final Trace trace = element.getAnnotation(Trace.class);
            return null != trace && (trace.ignoreAssertions() || type.desiredAssertionStatus());
        }
    }
}
