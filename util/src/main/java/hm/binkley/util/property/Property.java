package hm.binkley.util.property;

import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static lombok.AccessLevel.PRIVATE;

/**
 * {@code Property} represents getter/setter backed by a single thing. This
 * could be to a field, getter/setter method or function pair, an array or
 * list element, a map entry, etc.
 * <p>
 * Create properties with fluent builders: <table><tr><th>Backing type</th>
 * <th>Factory method</th></tr> <tr><td>Method or function</td> <td>{@link
 * #getter(Getter)}</td> </tr> <tr><td>Object with method or function</td>
 * <td>{@link #on(Object)}</td></tr> <tr><td>Array</td> <td>{@link
 * #in(Object[])}</td></tr> <tr><td>List</td> <td>{@link #in(List)}</td></tr>
 * <tr><td>Map</td> <td>{@link #in(Map)}</td></tr></table>
 *
 * @param <T> the property type
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley</a>
 */
public interface Property<T>
        extends Getter<T>, Setter<T> {
    /**
     * Gets the property value mapped by <var>mapper</var>.
     *
     * @param mapper the value mapper, never {@code null}
     * @param <U> the mapped value type
     *
     * @return the mapped value
     */
    @Nullable
    default <U> U map(
            @Nonnull final Function<? super T, ? extends U> mapper) {
        return mapper.apply(get());
    }

    /**
     * Maps the property onto another.
     *
     * @param mapper the property mapper, never {@code null}
     * @param <U> the mapped property type
     *
     * @return the mapped property, never {@code null}
     */
    @Nonnull
    default <U> Property<U> flatMap(@Nonnull
    final Function<? super T, ? extends Property<U>> mapper) {
        return mapper.apply(get());
    }

    /**
     * Views the property as another.
     *
     * @param toU the mapper to the other value, never {@code null}
     * @param toT the mapper from the other value, never {@code null}
     * @param <U> the other property type
     *
     * @return the other property, never {@code null}
     */
    @Nonnull
    default <U> Property<U> view(@Nonnull final Function<T, U> toU,
            @Nonnull final Function<U, T> toT) {
        return getter(() -> toU.apply(get())).
                setter(value -> set(toT.apply(value)));
    }

    /**
     * Produces a property directly stored with initial {@code null} value.
     *
     * @param <T> the property type
     *
     * @return the property, never {@code null}
     */
    @Nonnull
    static <T> Property<T> empty() {
        return DirectProperty.valueOf(null);
    }

    /**
     * Produces a property directly stored with initial <var>value</var>.
     *
     * @param value the initial value
     * @param <T> the property type
     *
     * @return the property, never {@code null}
     */
    @Nonnull
    static <T> Property<T> valueOf(@Nullable final T value) {
        return DirectProperty.valueOf(value);
    }

    /**
     * Starts a fluent builder for a property backed by a getter/setter
     * method or function pair.  Example: <pre>
     * AtomicReference&lt;String&gt; backing = new AtomicReference&lt;&gt;("Bob");
     * Property&lt;String&gt; p = getter(backing::get).
     *     setter(backing::set);</pre>
     *
     * @param getter the getter, never {@code null}
     * @param <T> the property type
     *
     * @return the fluent builder, never {@code null}
     */
    @Nonnull
    static <T> FromFunction<T> getter(@Nonnull final Getter<T> getter) {
        return new FromFunction<>(getter);
    }

    @RequiredArgsConstructor(access = PRIVATE)
    final class FromFunction<T> {
        @Nonnull
        private final Getter<T> getter;

        /**
         * Ends the fluent builder, producing a property backed by a method or
         * function pair.
         *
         * @param setter the setter, never {@code null}
         *
         * @return the property, never {@code null}
         */
        @Nonnull
        public Property<T> setter(@Nonnull final Setter<T> setter) {
            return new FunctionProperty<>(getter, setter);
        }
    }

    /**
     * Starts a fluent builder for a property backed by an object.
     * Example: <pre>
     * &#64;AllArgsConstructor
     * &#64;ToString
     * class X {
     *     String s;
     * }
     * <p>
     * X x = new X("Bob");
     * Property&lt;String&gt; p = on(x).
     *     getter(on -&gt; on.s).
     *     setter((on, value) -&gt; on.s = value);</pre>
     *
     * @param o the object, never {@code null}
     * @param <U> the object type
     *
     * @return the fluent builder, never {@code null}
     */
    @Nonnull
    static <U> FromObject<U> on(@Nonnull final U o) {
        return new FromObject<>(o);
    }

    @RequiredArgsConstructor(access = PRIVATE)
    final class FromObject<U> {
        @Nonnull
        private final U o;

        /**
         * Continues the fluent builder.
         *
         * @param getter the getter, never {@code null}
         * @param <T> the property type
         *
         * @return the fluent builder, never {@code null}
         */
        @Nonnull
        public <T> FromGetter<T> getter(
                @Nonnull final Function<U, T> getter) {
            return new FromGetter<>(getter);
        }

        @RequiredArgsConstructor(access = PRIVATE)
        public final class FromGetter<T> {
            @Nonnull
            private final Function<U, T> getter;

            /**
             * Ends the fluent builder, producing a property backed by an
             * object.
             *
             * @param setter the setter, never {@code null}
             *
             * @return the property, never {@code null}
             */
            @Nonnull
            Property<T> setter(@Nonnull final BiConsumer<U, T> setter) {
                return new ObjectProperty<>(o, getter, setter);
            }
        }
    }

    /**
     * Starts a fluent builder for a property backed by an array element.
     * Example: <pre>
     * Property&lt;String&gt; p = in("Bob").at(0);</pre>
     *
     * @param array the array, never {@code null}
     * @param <T> the property type
     *
     * @return the fluent builder, never {@code null}
     */
    @Nonnull
    @SafeVarargs
    static <T> FromArray<T> in(@Nonnull final T... array) {
        return new FromArray<>(array);
    }

    @RequiredArgsConstructor(access = PRIVATE)
    final class FromArray<T> {
        private final T[] array;

        /**
         * Ends the fluent builder, producing a property backed by an array
         * element.
         *
         * @param index the array index
         *
         * @return the property, never {@code null}
         */
        @Nonnull
        public Property<T> at(final int index) {
            return new ArrayProperty<>(array, index);
        }
    }

    /**
     * Starts a fluent builder for a property backed by a list element.
     * Example: <pre>
     * List&lt;String&gt; list = new ArrayList&lt;&gt();
     * list.add("Bob");
     * Property&lt;String&gt; p = in(list).at(0);</pre>
     *
     * @param list the list, never {@code null}
     * @param <T> the property type
     *
     * @return the fluent builder, never {@code null}
     */
    @Nonnull
    static <T> FromList<T> in(@Nonnull final List<T> list) {
        return new FromList<>(list);
    }

    @RequiredArgsConstructor(access = PRIVATE)
    final class FromList<T> {
        private final List<T> list;

        /**
         * Ends the fluent builder, producing a property backed by a list
         * element.
         *
         * @param index the list index
         *
         * @return the property, never {@code null}
         */
        @Nonnull
        public Property<T> at(final int index) {
            return new ListProperty<>(list, index);
        }
    }

    /**
     * Starts a fluent builder for a property backed by a map element.
     * Example: <pre>
     * Map&lt;Integer, String&gt; map = new HashMap&lt;&gt;();
     * map.put(123, "Bob");
     * Property&lt;String&gt; p = in(map).at(123);</pre>
     *
     * @param map the map, never {@code null}
     * @param <T> the property type
     * @param <K> the map key type
     *
     * @return the fluent builder, never {@code null}
     */
    @Nonnull
    static <T, K> FromMap<T, K> in(@Nonnull final Map<? super K, T> map) {
        return new FromMap<>(map);
    }

    @RequiredArgsConstructor(access = PRIVATE)
    final class FromMap<T, K> {
        private final Map<? super K, T> map;

        /**
         * Ends the fluent builder, producing a property backed by a map
         * entry.
         *
         * @param key the map key, never {@code null}
         *
         * @return the property, never {@code null}
         */
        @Nonnull
        public Property<T> at(@Nonnull final K key) {
            return new MapProperty<>(map, key);
        }
    }
}
