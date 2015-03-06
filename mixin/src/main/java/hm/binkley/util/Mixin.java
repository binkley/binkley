/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static hm.binkley.util.Mixin.CallStrategy.findFirst;
import static java.lang.reflect.Proxy.newProxyInstance;

/**
 * {@code Mixin} implements concrete class mixins via JDK proxies.  Proxy
 * handler strategy tries: <ol><li>Previously matched methods</li> <li>Exact
 * static match by enclosing type</li> <li>"Duck" matches by name and
 * parameter type</li></ol>  Mixins expose a single, <em>composite
 * interface</em> of all supported interfaces.
 * <p>
 * Mixins may optionally include {@code Mixin} as port of the <em>composite
 * interface</em>.  This exposes {@link #mixinDelegates()} providing public
 * access to the mixed in object delegated to by this proxy in the same order
 * as method lookup considers delegates.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @see #newMixin(Class, Object...) Create a new mixin
 */
public interface Mixin {
    /**
     * Creates a new mixin.  If <var>as</var> extends {@code Mixin}, provides
     * a supporting {@link #mixinDelegates()} method giving public access to
     * <var>delegates</var>.
     *
     * @param as a superinterface of visible public methods implemented by
     * <var>delegates</var>, never missing
     * @param delegates the mixed-in delegates (implementations)
     * @param <T> the mixin proxy type (type of <var>as</var>)
     *
     * @return the mixed-in proxy, never missing
     */
    @Nonnull
    static <T> T newMixin(@Nonnull final Class<T> as,
            final Object... delegates) {
        return newMixin(as, findFirst(), delegates);
    }

    @Nonnull
    static <T> T newMixin(@Nonnull final Class<T> as,
            @Nonnull final CallStrategy calling, final Object... delegates) {
        return as.cast(newProxyInstance(as.getClassLoader(), new Class[]{as},
                new MixinHandler<>(as, calling,
                        new MixedDelegates(delegates).mixinDelegates())));
    }

    /**
     * Gets an unmodifiable list of delegates implementing the mixin in the
     * same order as method lookup.
     *
     * @return the unmodifiable list of delegates, never missing
     */
    @Nonnull
    List<Object> mixinDelegates();

    @FunctionalInterface
    interface CallStrategy {
        static CallStrategy findFirst() {
            return Stream::findFirst;
        }

        /**
         * Calls through the given stream of <var>delegates</var>, returning a
         * value.  If no delegate is suitable to compute a value, returns
         * {@code null}.  Returns {@code Optional#empty()} to signal suitable
         * call sites with a computed result of {@code null}.
         * <p>
         * Delegates are possibly bound to an object instance, but are not
         * bound to call site arguments.
         *
         * @param delegates stream of potential call sites, never missing
         *
         * @return the computed call site, possibly empty if none suitable
         */
        @Nonnull
        Optional<MethodHandle> call(
                @Nonnull final Stream<MethodHandle> delegates);
    }
}
