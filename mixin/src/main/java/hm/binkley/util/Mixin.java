/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.lang.String.format;
import static java.lang.reflect.Proxy.newProxyInstance;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

/**
 * {@code Mixin} implements concrete class mixins via JDK proxies.  Proxy handler strategy tries:
 * <ol><li>Previously matched methods</li> <li>Exact static match by enclosing type</li> <li>"Duck"
 * matches by name and parameter type</li></ol>  Mixins expose a single, <em>composite
 * interface</em> of all supported interfaces.
 *
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

        private Factory() {
        }

        private static class Handler<T>
                implements InvocationHandler {
            private static final Lookup LOOKUP = MethodHandles.lookup();
            private final ConcurrentMap<Method, MethodHandle> matches;
            private final List<Object> delegates;

            private Handler(final Class<T> as, final List<Object> delegates) {
                this.delegates = delegates(as, delegates);
                matches = new ConcurrentHashMap<>(as.getMethods().length);
            }

            private List<Object> delegates(final Class<T> as, final List<Object> delegates) {
                for (final Method method : as.getMethods()) {
                    if (method.isDefault()) try {
                        final List<Object> delegatesPlus = new ArrayList<>(delegates.size() + 1);
                        delegatesPlus.addAll(delegates);
                        delegatesPlus.add(InterfaceInstance.newInstance(as));
                        return delegatesPlus;
                    } catch (final IllegalAccessException e) {
                        final IllegalAccessError x = new IllegalAccessError(e.getMessage());
                        x.setStackTrace(e.getStackTrace());
                        throw x;
                    } catch (final ClassNotFoundException e) {
                        final UnknownError x = new UnknownError(e.getMessage());
                        x.setStackTrace(e.getStackTrace());
                        throw x;
                    } catch (final InstantiationException e) {
                        final InstantiationError x = new InstantiationError(e.getMessage());
                        x.setStackTrace(e.getStackTrace());
                        throw x;
                    }
                }
                return delegates;
            }

            @Override
            public Object invoke(@Nonnull final Object proxy, @Nonnull final Method method,
                    final Object[] args)
                    throws Throwable {
                // Try as previous found - method match memoized
                final MethodHandle handle = matches.get(method);
                if (null != handle)
                    return handle.invoke(args);
                final MethodHandle unreflected = LOOKUP.unreflect(method);
                // Try as an implementation - static typing
                for (final Object delegate : delegates)
                    try {
                        final MethodHandle bound = LOOKUP
                                .findVirtual(delegate.getClass(), method.getName(),
                                        unreflected.type()).bindTo(delegate);
                        final Object value = bound.invokeWithArguments(args);
                        matches.put(method, bound);
                        return value;
                    } catch (final NoSuchMethodException e) {
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
                        final MethodHandle bound = LOOKUP.unreflect(quack).bindTo(delegate);
                        final Object value = bound.invokeWithArguments(args);
                        matches.put(method, bound);
                        return value;
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
                this.mixed = unmodifiableList(mixed);
            }

            @SuppressWarnings("ReturnOfCollectionOrArrayField")
            @Nonnull
            @Override
            public List<Object> mixinDelegates() {
                return mixed;
            }
        }
    }
}
