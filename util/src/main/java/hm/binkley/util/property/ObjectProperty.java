package hm.binkley.util.property;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Delegate;

@EqualsAndHashCode
@RequiredArgsConstructor
@ToString
final class ObjectProperty<T>
        implements Property<T> {
    private final Object on;
    @Delegate
    private final Getter<T> getter;
    @Delegate
    private final Setter<T> setter;
}
