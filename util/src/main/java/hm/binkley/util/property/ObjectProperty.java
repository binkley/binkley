package hm.binkley.util.property;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;
import java.util.function.Function;

@EqualsAndHashCode
@RequiredArgsConstructor
@ToString
final class ObjectProperty<T, U>
        implements Property<T> {
    private final U on;
    private final Function<U, T> getter;
    private final BiConsumer<U, T> setter;

    @Nonnull
    @Override
    public T get() {
        return getter.apply(on);
    }

    @Override
    public void set(@Nonnull final T value) {
        setter.accept(on, value);
    }
}
