package hm.binkley.util.function;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static hm.binkley.util.function.Matching.matching;
import static hm.binkley.util.function.MatchingTest.B.B;
import static hm.binkley.util.function.MatchingTest.Ex.ONE;
import static hm.binkley.util.function.MatchingTest.Ex.TWO;
import static java.util.function.Function.identity;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public final class MatchingTest {
    @Test
    public void shouldMatchExactValueForPrimitive() {
        assertThat(matching(Integer.class).
                when(1).then(x -> 2).
                apply(1).get(), equalTo(2));
    }

    @Test
    public void shouldMatchExactValueForObject() {
        assertThat(matching(Ex.class).
                when(ONE).then(Ex::next).
                apply(ONE).get(), equalTo(TWO));
    }

    @Test
    public void shouldMatchWithFunction() {
        assertThat(matching(Integer.class, Object.class).
                when(eq(2)).thenThrow(TestException::new).
                when(eq(0)).then(x -> 1).
                apply(0).get(), equalTo(1));
    }

    @Test
    public void shouldDefaultValue() {
        assertThat(matching(Integer.class, Object.class).
                otherwise(1).
                apply(0).get(), equalTo(1));
    }

    @Test
    public void shouldDefaultSuppliedValue() {
        assertThat(matching(Integer.class, Object.class).
                otherwise(() -> 1).
                apply(0).get(), equalTo(1));
    }

    @Test
    public void shouldThrowFromException() {
        final TestException otherwise = new TestException();
        try {
            matching(Integer.class, Object.class).
                    otherwiseThrow(otherwise).
                    apply(0);
            fail("Did not throw");
        } catch (final TestException e) {
            assertThat(e, is(sameInstance(otherwise)));
        }
    }

    @Test(expected = TestException.class)
    public void shouldThrowFromSupplier() {
        matching(Integer.class, Object.class).
                otherwiseThrow(TestException::new).
                apply(0);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowWhenNoMatch() {
        matching(Integer.class, Integer.class).
                apply(0);
    }

    @Test
    public void shouldType() {
        assertThat(matching(C.class, C.class).
                when(isA(A.class)).then(identity()).
                when(isA(B.class)).then(identity()).
                apply(B).get(), equalTo(B));
    }

    @Test
    public void shouldSideEffect() {
        final AtomicInteger i = new AtomicInteger();
        matching(Integer.class, Void.class).
                when(eq(1)).then(i::set).
                apply(1);
        assertThat(i.get(), equalTo(1));
    }

    @Test
    public void shouldBeLazyUntilApplyAndDoNothing() {
        matching(Object.class, Void.class).
                otherwiseThrow(TestException::new);
        // Nothing to assert - just that it does not throw
    }

    @Test
    public void shouldSupportHamcrestMatcher() {
        assertThat(matching(Integer.class).
                when(equalTo(1)).then(0).
                apply(1).get(), equalTo(0));
    }

    @Test
    public void shouldCleanUpStackForThenThrowFromSupplier() {
        try {
            matching(Integer.class, Void.class).
                    when(eq(1)).thenThrow(TestException::new).
                    apply(1);
            fail("Did not throw");
        } catch (final TestException e) {
            assertThat(firstFrameOf(e).getClassName(),
                    equalTo(getClass().getName()));
            assertThat(firstFrameOf(e).getMethodName(),
                    equalTo("shouldCleanUpStackForThenThrowFromSupplier"));
        }
    }

    @Test
    public void shouldCleanUpStackForOtherwiseThrowFromSupplier() {
        try {
            matching(Object.class, Void.class).
                    otherwiseThrow(TestException::new).
                    apply(1);
            fail("Did not throw");
        } catch (final TestException e) {
            // TODO: Varies by JDK version - could be class or lambda
            assertThat(firstFrameOf(e).getClassName(),
                    startsWith(getClass().getName()));
            assertThat(firstFrameOf(e).getMethodName(),
                    equalTo("shouldCleanUpStackForOtherwiseThrowFromSupplier"));
        }
    }

    private static Predicate<Object> isA(final Class<?> type) {
        return o -> type.isAssignableFrom(o.getClass());
    }

    private static Predicate<Integer> eq(final int n) {
        return m -> n == m;
    }

    private static StackTraceElement firstFrameOf(final Throwable t) {
        return t.getStackTrace()[0];
    }

    abstract static class Ex {
        static final Ex ONE = new Ex() {
            @Override
            Ex next() {
                return TWO;
            }
        };
        static final Ex TWO = new Ex() {
            @Override
            Ex next() {
                return TRE;
            }
        };
        static final Ex TRE = new Ex() {
            @Override
            Ex next() {
                return ONE;
            }
        };

        private Ex() {
        }

        abstract Ex next();
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
