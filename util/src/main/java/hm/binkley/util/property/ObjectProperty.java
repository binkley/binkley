package hm.binkley.util.property;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Function;

@EqualsAndHashCode
@RequiredArgsConstructor
@ToString
final class ObjectProperty<T, U>
        implements Property<T> {
    @Nonnull
    private final U on;
    @Nonnull
    private final Function<U, T> getter;
    @Nonnull
    private final BiConsumer<U, T> setter;

    @Nullable
    @Override
    public T get() {
        return getter.apply(on);
    }

    @Override
    public void set(@Nullable final T value) {
        setter.accept(on, value);
    }
}
