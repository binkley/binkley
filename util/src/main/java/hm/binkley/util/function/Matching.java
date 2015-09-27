package hm.binkley.util.function;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static lombok.AccessLevel.PRIVATE;

/**
 * {@code Matching} represents <a href="https://en.wikipedia.org/wiki/Pattern_matching">Pattern
 * Matching</a> in Java as a function production an optional.  Example: <pre>
 * asList(0, 1, 2, 3, 13, 14, null, -1).stream().
 *         peek(n -> out.print(format("%d -> ", n))).
 *         map(matching(Integer.class, Object.class).
 *             when(Objects::isNull).then(n -&gt; "!").
 *             when(is(0)).then(nil()).
 *             when(is(1)).then("one").
 *             when(is(13)).then(() -&gt; "unlucky").
 *             when(is(14)).then(printIt()).
 *             when(even()).then(scaleBy(3)).
 *             when(gt(2)).then(dec()).
 *             none().then("no match")).
 *         map(MatchingTest::toString).
 *         forEach(out::println);</pre>
 * <p>
 * <i>NB</i> &mdash; There is no way to distinguish from an empty optional if
 * there was no match, or if a match mapped the input to {@code null}, without
 * use of a {@link When#then(Object) sentinel value} or {@link
 * When#thenThrow(Supplier) thrown exception}.
 * <p>
 * <strong>NB</strong> &mdash; There is no formal destructuring, but this can
 * be simulated in the {@code Predicate} to {@link #when(Predicate) when}.
 *
 * @param <T> the input type to match against
 * @param <U> the output type of a matched pattern
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 */
@NoArgsConstructor(access = PRIVATE)
public final class Matching<T, U>
        implements Function<T, Optional<U>> {
    private final Collection<Case> cases = new ArrayList<>();

    /**
     * Begins pattern matching with a new pattern matcher.
     *
     * @param inType the input type token, never {@code null}
     * @param outType the output type token, never {@code null}
     * @param <T> the input type to match against
     * @param <U> the output type of a matched pattern
     *
     * @return the pattern matcher, never {@code null}
     *
     * @todo Avoid the type tokens
     */
    public static <T, U> Matching<T, U> matching(final Class<T> inType,
            final Class<U> outType) {
        return new Matching<>();
    }

    /**
     * Begins a when/then pair.
     *
     * @param when the pattern matching test, never {@code null}
     *
     * @return the pattern continuance, never {@code null}
     */
    public When when(final Predicate<? super T> when) {
        return new When(when);
    }

    /**
     * Begins a default when/then pair, always placed <strong>last</strong> in
     * the list of cases (evaluates no cases after this one).
     *
     * @return then pattern continuance, never {@code null}
     */
    public When none() {
        return when(o -> true);
    }

    /**
     * Evaluates the pattern matching.
     *
     * @param in the input to match against, possibly {@code null}
     *
     * @return the match result (empty if no match), never {@code null}
     */
    @Override
    public Optional<U> apply(final T in) {
        return cases.stream().
                filter(c -> c.p.test(in)).
                findFirst().
                map(c -> c.q.apply(in));
    }

    @RequiredArgsConstructor(access = PRIVATE)
    public final class When {
        /**
         * Number of frames to discard when creating an exception for a match.
         * Very sensitive to implementation.  This aids in understanding stack
         * traces from matching, discarding internal machinery and leaving the
         * actual throwing call at the top of the stack.
         */
        private static final int N = 7;
        private final Predicate<? super T> when;

        /**
         * Ends a when/then pair, evaluating <var>then</var> against the input
         * if matched.
         *
         * @param then the pattern matching function, never {@code null}
         *
         * @return the pattern matcher, never {@code null}
         */
        public Matching<T, U> then(
                final Function<? super T, ? extends U> then) {
            cases.add(new Case(when, then));
            return Matching.this;
        }

        /**
         * Ends a when/then pair, returning <var>then</var> if matched.
         *
         * @param then the pattern matching value, possibly {@code null}
         *
         * @return the pattern matcher, never {@code null}
         */
        public Matching<T, U> then(final U then) {
            cases.add(new Case(when, x -> then));
            return Matching.this;
        }

        /**
         * Ends a when/then pair, evaluating <var>then</var> independent of
         * supplier if matched.
         *
         * @param then the pattern matching supplier, never {@code null}
         *
         * @return the pattern matcher, never {@code null}
         */
        public Matching<T, U> then(final Supplier<? extends U> then) {
            cases.add(new Case(when, x -> then.get()));
            return Matching.this;
        }

        /**
         * Ends a when/then pair, evaluating <var>then</var> to {@code null}
         * if matched.
         *
         * @param then the input consumer, never {@code null}
         *
         * @return the pattern matcher, never {@code null}
         */
        public Matching<T, U> then(final Consumer<? super T> then) {
            cases.add(new Case(when, o -> {
                then.accept(o);
                return null;
            }));
            return Matching.this;
        }

        /**
         * Ends a when/then pair, evaluating <var>then</var> independent of
         * supplier and throwing the new exception if matched.
         *
         * @param then the pattern matching exception supplier, never {@code
         * null}
         *
         * @return the pattern matcher, never {@code null}
         */
        public Matching<T, U> thenThrow(
                final Supplier<RuntimeException> then) {
            cases.add(new Case(when, x -> {
                final RuntimeException e = then.get();
                final List<StackTraceElement> stack = asList(
                        e.getStackTrace());
                e.setStackTrace(stack.subList(N, stack.size()).
                        toArray(new StackTraceElement[stack.size() - N]));
                throw e;
            }));
            return Matching.this;
        }
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private final class Case {
        private final Predicate<? super T> p;
        private final Function<? super T, ? extends U> q;
    }
}
