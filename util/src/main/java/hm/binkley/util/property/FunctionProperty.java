package hm.binkley.util.property;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Delegate;

import javax.annotation.Nonnull;

@EqualsAndHashCode
@RequiredArgsConstructor
@ToString
final class FunctionProperty<T>
        implements Property<T> {
    @Delegate
    @Nonnull
    private final Getter<T> getter;
    @Delegate
    @Nonnull
    private final Setter<T> setter;
}
