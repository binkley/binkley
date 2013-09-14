package hm.binkley.util;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import java.util.List;

import static com.google.common.collect.Iterables.filter;
import static hm.binkley.util.EgXnum.OfType.ofType;

/**
 * {@code EgXnum} needs documentation.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public abstract class EgXnum<T>
        extends Xnum<EgXnum<T>> {
    public static final Foo FOO = new Foo();
    public static final Bar BAR = new Bar();
    private static final List<EgXnum<?>> VALUES = ImmutableList.<EgXnum<?>>of(FOO, BAR);
    private static int ORDINAL;

    private EgXnum(@Nonnull final String name) {
        super(name, ORDINAL++);
    }

    @Nonnull
    public static List<EgXnum<?>> values() {
        return VALUES;
    }

    @Nonnull
    public static <T> List<EgXnum<T>> valuesOfType(@Nonnull final Class<T> type) {
        return ImmutableList.<EgXnum<T>>copyOf(filter(values(), ofType(type)));
    }

    public abstract T get();

    @Nonnull
    public abstract Class<T> type();

    private static final class Foo
            extends EgXnum<Integer> {
        private Foo() {
            super("Foo");
        }

        @Override
        public Integer get() {
            return 13;
        }

        @Nonnull
        @Override
        public Class<Integer> type() {
            return Integer.class;
        }
    }

    private static final class Bar
            extends EgXnum<String> {
        private Bar() {
            super("Bar");
        }

        @Override
        public String get() {
            return "Friday";
        }

        @Nonnull
        @Override
        public Class<String> type() {
            return String.class;
        }
    }

    static class OfType
            implements Predicate<EgXnum<?>> {
        private final Class<?> type;

        private OfType(final Class<?> type) {
            this.type = type;
        }

        @Nonnull
        static OfType ofType(final Class<?> type) {
            return new OfType(type);
        }

        @Override
        public boolean apply(final EgXnum<?> xnum) {
            return type == xnum.type();
        }
    }
}
