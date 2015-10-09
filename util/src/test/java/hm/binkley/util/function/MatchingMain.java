package hm.binkley.util.function;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static hm.binkley.util.Bug.bug;
import static hm.binkley.util.function.Matching.matching;
import static java.lang.String.format;
import static java.lang.System.out;
import static org.hamcrest.Matchers.instanceOf;

/**
 * {@code MatchingTest} demonstrates {@code Matching}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public final class MatchingMain {
    public static void main(final String... args) {
        out.println("- Various matching cases:");
        Stream.of(0, 1, 2, 3, 13, 14, 15, null, -1).
                peek(n -> out.print(format("%d -> ", n))).
                map(matching(Integer.class, Object.class).
                        when(Objects::isNull).then(n -> "BANG!").
                        when(0).then(nil()).
                        when(eq(1)).then("one").
                        when(eq(13)).then(() -> "unlucky").
                        when(eq(14)).then(printIt()).
                        when(eq(15)).then(printIt(), 6.28318f).
                        when(even()).then(scaleBy(3)).
                        when(gt(2)).then(dec()).
                        when(instanceOf(Float.class)).then(nil()).
                        when(i -> {
                            switch (i) {
                            default:
                                return false;
                            }
                        }).thenThrow(bug("Cannot reach here")).
                        otherwise("no match")).
                map(MatchingMain::toString).
                forEach(out::println);

        try {
            out.println();
            out.println("- Matching case throwing an exception:");
            matching(Integer.class, Void.class).
                    when(eq(1)).thenThrow(TestException::new).
                    apply(1);
        } catch (final TestException e) {
            e.printStackTrace(out);
        }

        try {
            out.println();
            out.println("- Default case throwing an exception:");
            matching(Integer.class, Void.class).
                    otherwiseThrow(TestException::new).
                    apply(1);
        } catch (final TestException e) {
            e.printStackTrace(out);
        }

        try {
            out.println();
            out.println("- Invalid input with no matching case or default:");
            matching(Integer.class, Void.class).
                    apply(1);
        } catch (final IllegalStateException e) {
            e.printStackTrace(out);
        }
    }

    private static Predicate<Integer> eq(final int n) {
        return m -> n == m;
    }

    private static <U> Supplier<U> nil() {
        return () -> null;
    }

    private static Consumer<Integer> printIt() {
        return out::println;
    }

    private static Predicate<Integer> even() {
        return n -> 0 == n % 2;
    }

    private static Function<Integer, Integer> scaleBy(final int factor) {
        return n -> n * factor;
    }

    private static Predicate<Integer> gt(final int b) {
        return n -> b < n;
    }

    private static Function<Integer, Integer> dec() {
        return n -> n - 1;
    }

    private static String toString(final Optional<Object> o) {
        return format("%s (%s)", o, o.map(Object::getClass));
    }

    interface C {
    }

    enum A
            implements C {
    }

    enum B
            implements C {
        B
    }

    static final class TestException
            extends RuntimeException {
    }
}
