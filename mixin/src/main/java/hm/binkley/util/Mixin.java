/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.lang.String.format;
import static java.lang.reflect.Proxy.newProxyInstance;
import static java.util.Arrays.asList;

/**
 * {@code Mixin} implements concrete class mixins via JDK proxies.  Proxy handler strategy tries:
 * <ol><li>Previously matched methods</li> <li>Exact static match by enclosing type</li> <li>"Duck"
 * matches by name and parameter type</li></ol>  Mixins expose a single, <em>composite
 * interface</em> of all supported interfaces.
 * <p/>
 * Mixins may optionally include {@code Mixin} as port of the <em>composite interface</em>.  This
 * exposes {@link #mixinDelegates()} providing public access to the mixed in object delegated to by
 * this proxy.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public interface Mixin {
    /**
     * Gets an unmodifiable list of delegates implementing the mixin.
     *
     * @return the unmodifiable list of delegates, never missing
     */
    @Nonnull
    List<Object> mixinDelegates();

    final class Factory {
        private Factory() {
        }

        /**
         * Creates a new mixin.  If <var>as</var> extends {@code Mixin}, provides a supporting
         * {@link #mixinDelegates()} method to give public access to <var>delegates</var>.
         *
         * @param as a superinterface of visible public methods implemented by <var>delegates</var>,
         * never missing
         * @param delegates the mixed-in delegates (implementations)
         * @param <T> the mixin proxy type (type of <var>as</var>)
         *
         * @return the mixed-in proxy, never missing
         */
        @Nonnull
        public static <T> T newMixin(@Nonnull final Class<T> as, final Object... delegates) {
            return as.cast(newProxyInstance(as.getClassLoader(), new Class[]{as},
                    new Handler<>(as, new MixedDelegates(delegates).mixinDelegates())));
        }

        private static final class Invoker {
            private final Method method;
            private final Object receiver;

            private Invoker(final Method method, final Object receiver) {
                this.method = method;
                this.receiver = receiver;
            }

            public Object invoke(final Object... args)
                    throws ReflectiveOperationException {
                return method.invoke(receiver, args);
            }

            public Throwable unwrap(final InvocationTargetException e) {
                final Throwable x = e.getTargetException();
                if (x instanceof RuntimeException || x instanceof Error || isThrowing(x))
                    return x;
                return e;
            }

            private boolean isThrowing(final Throwable e) {
                return asList(method.getExceptionTypes()).contains(e.getClass());
            }
        }

        private static class Handler<T>
                implements InvocationHandler {
            private final ConcurrentMap<Method, Invoker> matches;
            private final List<Object> delegates;

            private Handler(final Class<T> as, final List<Object> delegates) {
                matches = new ConcurrentHashMap<>(as.getMethods().length);
                this.delegates = delegates;
            }

            @Override
            public Object invoke(@Nonnull final Object proxy, @Nonnull final Method method,
                    final Object[] args)
                    throws Throwable {
                // Try as previous found - method match memoized
                final Invoker invoker = matches.get(method);
                if (null != invoker)
                    try {
                        return invoker.invoke(args);
                    } catch (final InvocationTargetException e) {
                        throw invoker.unwrap(e);
                    }
                // Try as an implementation - static typing
                for (final Object delegate : delegates)
                    try {
                        final Object value = method.invoke(delegate, args);
                        matches.put(method, new Invoker(method, delegate));
                        return value;
                    } catch (final InvocationTargetException e) {
                        final Invoker newInvoker = new Invoker(method, delegate);
                        matches.put(method, newInvoker);
                        throw newInvoker.unwrap(e);
                    } catch (final IllegalArgumentException ignored) {
                        // Try next method
                    }
                // Try by name/arg types - duck typing
                final String name = method.getName();
                final Class<?>[] types = method.getParameterTypes();
                for (final Object delegate : delegates) {
                    final Class<?> duck = delegate.getClass();
                    try {
                        final Method quack = duck.getMethod(name, types);
                        if (!method.getReturnType().isAssignableFrom(quack.getReturnType()))
                            continue;
                        final Invoker newInvoker = new Invoker(quack, delegate);
                        try {
                            matches.put(method, newInvoker);
                            return quack.invoke(delegate, args);
                        } catch (final InvocationTargetException e) {
                            // See http://amitstechblog.wordpress.com/2011/07/24/java-proxies-and-undeclaredthrowableexception/
                            throw newInvoker.unwrap(e);
                        }
                    } catch (final NoSuchMethodException ignored) {
                        // Try next delegate
                    }
                }

                throw new AbstractMethodError(
                        format("BUG: Missing implementation for <%s> among %s.", method,
                                delegates));
            }
        }

        private static class MixedDelegates
                implements Mixin {
            private final List<Object> mixed;

            public MixedDelegates(final Object... delegates) {
                final List<Object> mixed = new ArrayList<>(delegates.length + 1);
                mixed.addAll(asList(delegates));
                mixed.add(this);
                this.mixed = ImmutableList.copyOf(mixed);
            }

            @Nonnull
            @Override
            public List<Object> mixinDelegates() {
                return mixed;
            }
        }
    }
}
