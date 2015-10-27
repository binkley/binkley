/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util;

import javax.annotation.Nonnull;
import java.util.List;

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
        return as.cast(newProxyInstance(as.getClassLoader(), new Class[]{as},
                new MixinHandler<>(as,
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
}
