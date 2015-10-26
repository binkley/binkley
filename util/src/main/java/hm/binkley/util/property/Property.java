package hm.binkley.util.property;

import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

/**
 * {@code Property} represents getter/setter backed by a single thing. This
 * could be to a field, getter/setter method or function pair, an array or
 * list element, a map entry, etc.
 *
 * @param <T> the property type
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley</a>
 */
public interface Property<T>
        extends Getter<T>, Setter<T> {
    /**
     * Starts a fluent builder for a property backed by a getter/setter method
     * or function pair.
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

    @Nonnull
    static FromObject on(@Nonnull final Object o) {
        return new FromObject(o);
    }

    @RequiredArgsConstructor(access = PRIVATE)
    final class FromObject {
        @Nonnull
        private final Object o;

        @Nonnull
        public <T> FromGetter<T> getter(@Nonnull final Getter<T> getter) {
            return new FromGetter<>(getter);
        }

        @RequiredArgsConstructor(access = PRIVATE)
        public final class FromGetter<T> {
            @Nonnull
            private final Getter<T> getter;

            @Nonnull
            Property<T> setter(@Nonnull final Setter<T> setter) {
                return new ObjectProperty<>(o, getter, setter);
            }
        }
    }

    /**
     * Starts a fluent builder for a property backed by an array element.
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
