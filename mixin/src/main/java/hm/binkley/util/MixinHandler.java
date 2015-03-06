/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org/>.
 */

package hm.binkley.util;

import hm.binkley.util.Mixin.CallStrategy;
import lombok.SneakyThrows;

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

/**
 * {@code MixinHandler} implements method lookup for {@link
 * Mixin#newMixin(Class, Object...)}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
final class MixinHandler<T>
        implements InvocationHandler {
    private static final Lookup LOOKUP = MethodHandles.lookup();
    private final ConcurrentMap<Method, MethodHandle> matches;
    private final CallStrategy strategy;
    private final List<Object> delegates;

    MixinHandler(final Class<T> as, final CallStrategy strategy,
            final List<Object> delegates) {
        this.strategy = strategy;
        this.delegates = delegates(as, delegates);
        matches = new ConcurrentHashMap<>(as.getMethods().length);
    }

    @SneakyThrows({IllegalAccessException.class, ClassNotFoundException.class,
            InstantiationException.class})
    private List<Object> delegates(final Class<T> as,
            final List<Object> delegates) {
        for (final Method method : as.getMethods())
            if (method.isDefault()) {
                final List<Object> delegatesPlus = new ArrayList<>(
                        delegates.size() + 1);
                delegatesPlus.addAll(delegates);
                delegatesPlus.add(InterfaceInstance.newInstance(as));
                return delegatesPlus;
            }
        return delegates;
    }

    @Override
    public Object invoke(@Nonnull final Object proxy,
            @Nonnull final Method method, final Object[] args)
            throws Throwable {
        // Try as previous found - method match memoized
        final MethodHandle handle = matches.get(method);
        if (null != handle)
            return handle.invokeWithArguments(args);
        final MethodHandle unreflected = LOOKUP.unreflect(method);
        // Try as an implementation - static typing
        for (final Object delegate : delegates)
            try {
                final MethodHandle bound = LOOKUP.
                        findVirtual(delegate.getClass(), method.getName(),
                                unreflected.type()).
                        bindTo(delegate);
                return call(method, bound, args);
            } catch (final IllegalAccessException e) {
                if (!method.getDeclaringClass().
                        isAssignableFrom(delegate.getClass()))
                    continue;
                // Try reflection, less efficient but more general
                method.setAccessible(true);
                final MethodHandle bound = unreflected.bindTo(delegate);
                return call(method, bound, args);
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
                if (!method.getReturnType().
                        isAssignableFrom(quack.getReturnType()))
                    continue;
                final MethodHandle bound = LOOKUP.unreflect(quack).
                        bindTo(delegate);
                return call(method, bound, args);
            } catch (final NoSuchMethodException ignored) {
                // Try next delegate
            }
        }
        throw notFound(method);
    }

    private Object call(final @Nonnull Method method,
            final MethodHandle bound, final Object... args)
            throws Throwable {
        final Object value = bound.invokeWithArguments(args);
        matches.put(method, bound);
        return value;
    }

    @Nonnull
    private AbstractMethodError notFound(final @Nonnull Method method) {
        return new AbstractMethodError(
                format("BUG: Missing implementation for <%s> among %s.",
                        method, delegates));
    }
}
