package hm.binkley.util.function;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hamcrest.Matcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.Arrays.copyOfRange;
import static java.util.function.Predicate.isEqual;
import static java.util.regex.Pattern.compile;
import static lombok.AccessLevel.PRIVATE;

/**
 * {@code Matching} represents <a href="https://en.wikipedia.org/wiki/Pattern_matching">Pattern
 * Matching</a> in Java as a function production an optional.  Example: <pre>
 * Stream.of(0, 1, 2, 3, 13, 14, 15, null, -1).
 *         peek(n -> out.print(format("%d -> ", n))).
 *         map(matching(Integer.class, Object.class).
 *             when(Objects::isNull).then(n -&gt; "!").
 *             when(is(0)).then(nil()).
 *             when(is(1)).then("one").
 *             when(is(13)).then(() -&gt; "unlucky").
 *             when(is(14)).then(printIt()).
 *             when(is(15)).then(printIt(), 6.28318f).
 *             when(even()).then(scaleBy(3)).
 *             when(gt(2)).then(dec()).
 *             when(instanceOf(Float.class)).then(nil()).
 *             otherwise("no match")).
 *         map(MatchingTest::toString).
 *         forEach(out::println);</pre>
 * <p>
 * If there is no match, {@code Matching} complains with an {@code
 * IllegalStateException} and the message, "No match".
 * <p>
 * <strong>NB</strong> &mdash; There is no formal destructuring, but this can
 * be simulated in the {@code Predicate} to {@link #when(Predicate) when}.
 *
 * @param <T> the input type to match against
 * @param <U> the output type of a matched pattern
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Enforce type safety - prevent cases which are impossible
 */
@NoArgsConstructor(access = PRIVATE)
public final class Matching<T, U>
        implements Function<T, Optional<U>> {
    // Careful editing me - need to ignored stack frames starting with
    // ourselves or our inner classes, but NOT ignore our test class which
    // begins with us as a prefix
    private static final Pattern ignoreStackFrames = compile(
            format("^(java\\.|javax\\.|sun\\.|com\\.sun\\.|%s(\\$|$))",
                    Matching.class.getName()));

    private final List<Case> cases = new ArrayList<>();

    /**
     * Begins pattern matching with a new pattern matcher where input and
     * output types differ.
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
    @SuppressWarnings("UnusedParameters")
    public static <T, U> Matching<T, U> matching(
            @Nonnull final Class<T> inType, @Nonnull final Class<U> outType) {
        return new Matching<>();
    }

    /**
     * Begins pattern matching with a new pattern matcher where input and
     * output types are the same.
     *
     * @param type the input/output type token, never {@code null}
     * @param <T> the input/output type to match against
     *
     * @return the pattern matcher, never {@code null}
     *
     * @todo Avoid the type tokens
     */
    public static <T> Matching<T, T> matching(@Nonnull final Class<T> type) {
        return matching(type, type);
    }

    /**
     * Begins a when/then pair which checks object equality, a convenience for
     * the predicate, {@code () -> Objects.equals(input, value)}.
     *
     * @param value the value to match, possibly {@code null}
     *
     * @return the pattern continuace, never {@code null}
     *
     * @see Predicate#isEqual(Object)
     */
    public When when(@Nullable final T value) {
        return new When(isEqual(value));
    }

    /**
     * Begins a when/then pair.
     *
     * @param when the pattern matching test, never {@code null}
     *
     * @return the pattern continuance, never {@code null}
     */
    @Nonnull
    public When when(@Nonnull final Predicate<? super T> when) {
        return new When(when);
    }

    /**
     * Begins a when/then pair with a Hamcrest matcher.
     *
     * @param matcher the hamcrest matcher, never {@code null}
     *
     * @return the pattern continuance, never {@code null}
     */
    @Nonnull
    public When when(@Nonnull final Matcher<? super T> matcher) {
        return new When(matcher::matches);
    }

    /**
     * Convenience combination of a default when/then pair returning
     * <var>otherwise</var>, always placed <strong>last</strong> in the list
     * of cases.  Equivalent to: <pre>
     * when(__ -&gt; true).then(otherwise); // result value
     * </pre>
     *
     * @param otherwise the pattern matching value, possibly {@code null}
     *
     * @return the pattern matcher, never {@code null}
     */
    @Nonnull
    public Matching<T, U> otherwise(@Nullable final U otherwise) {
        return always().then(otherwise);
    }

    /**
     * Convenience combination of a default when/then pair returning the
     * evaluation of <var>otherwise</var>, always placed
     * <strong>last</strong>
     * in the list of cases.  Equivalent to: <pre>
     * when(__ -&gt; true).then(otherwise); // supplier of result value
     * </pre>
     *
     * @param otherwise the pattern matching supplier, never {@code null}
     *
     * @return the pattern matcher, never {@code null}
     */
    @Nonnull
    public Matching<T, U> otherwise(
            @Nonnull final Supplier<? extends U> otherwise) {
        return always().then(otherwise);
    }

    /**
     * Convenience combination of a default when/then pair throwing the
     * evaluation of <var>otherwise</var>, always placed
     * <strong>last</strong>
     * in the list of cases.  Equivalent to: <pre>
     * when(__ -&gt; true).thenThrow(otherwise); // exception
     * </pre>
     *
     * @param otherwise the pattern matching exception, never {@code null}
     *
     * @return the pattern matcher, never {@code null}
     */
    @Nonnull
    public Matching<T, U> otherwiseThrow(
            @Nonnull final RuntimeException otherwise) {
        return always().thenThrow(otherwise);
    }

    /**
     * Convenience combination of a default when/then pair throwing the
     * evaluation of <var>otherwise</var>, always placed
     * <strong>last</strong>
     * in the list of cases.  Equivalent to: <pre>
     * when(__ -&gt; true).thenThrow(otherwise); // supplier of exception
     * </pre>
     *
     * @param otherwise the pattern matching exception supplier, never {@code
     * null}
     *
     * @return the pattern matcher, never {@code null}
     */
    @Nonnull
    public Matching<T, U> otherwiseThrow(
            @Nonnull final Supplier<? extends RuntimeException> otherwise) {
        return always().thenThrow(otherwise);
    }

    /**
     * Evaluates the pattern matching.
     *
     * @param in the input to match against, possibly {@code null}
     *
     * @return the match result (empty if no match), never {@code null}
     *
     * @throws IllegalStateException if no match found
     */
    @Override
    @Nonnull
    public Optional<U> apply(@Nullable final T in)
            throws IllegalStateException {
        final Optional<Case> match = cases.stream().
                filter(c -> c.p.test(in)).
                findFirst();
        if (!match.isPresent())
            cleanAndThrow(new IllegalStateException("No match"));
        return match.
                map(c -> c.q.apply(in));
    }

    private When always() {
        return when(__ -> true);
    }

    /**
     * Tricky as we want to clean up the stack trace to point at the use of
     * "when" and friends, but cannot use a fixed count of frames to ignore as
     * the exact details are JVM-make and -version dependent.
     */
    private static <U, E extends Exception> U cleanAndThrow(final E e)
            throws E {
        final StackTraceElement[] frames = e.getStackTrace();
        int i = 0;
        while (ignoreStackFrames.matcher(frames[i].getClassName()).find())
            ++i;
        e.setStackTrace(copyOfRange(frames, i, frames.length));
        throw e;
    }

    @RequiredArgsConstructor(access = PRIVATE)
    public final class When {
        private final Predicate<? super T> when;

        /**
         * Ends a when/then pair, evaluating <var>then</var> against the input
         * if matched.
         *
         * @param then the pattern matching function, never {@code null}
         *
         * @return the pattern matcher, never {@code null}
         */
        @Nonnull
        public Matching<T, U> then(
                @Nonnull final Function<? super T, ? extends U> then) {
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
        @Nonnull
        public Matching<T, U> then(@Nullable final U then) {
            cases.add(new Case(when, __ -> then));
            return Matching.this;
        }

        /**
         * Ends a when/then pair, evaluating <var>then</var> to the supplier
         * evaluation if matched.
         *
         * @param then the pattern matching supplier, never {@code null}
         *
         * @return the pattern matcher, never {@code null}
         */
        @Nonnull
        public Matching<T, U> then(
                @Nonnull final Supplier<? extends U> then) {
            cases.add(new Case(when, __ -> then.get()));
            return Matching.this;
        }

        /**
         * Ends a when/then pair, evaluating <var>then</var> to
         * <var>value</var> if matched.
         *
         * @param then the input consumer, never {@code null}
         * @param value the result value, possibly {@code null}
         *
         * @return the pattern matcher, never {@code null}
         */
        @Nonnull
        public Matching<T, U> then(@Nonnull final Consumer<? super T> then,
                @Nullable final U value) {
            cases.add(new Case(when, o -> {
                then.accept(o);
                return value;
            }));
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
        @Nonnull
        public Matching<T, U> then(@Nonnull final Consumer<? super T> then) {
            return then(then, null);
        }

        /**
         * Ends a when/then pair, evaluating <var>then</var> independent of
         * value and throwing the new exception if matched.
         *
         * @param then the pattern matching exception, never {@code null}
         *
         * @return the pattern matcher, never {@code null}
         */
        @Nonnull
        public Matching<T, U> thenThrow(
                @Nonnull final RuntimeException then) {
            cases.add(new Case(when, __ -> {
                throw then;
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
        @Nonnull
        public Matching<T, U> thenThrow(
                @Nonnull final Supplier<? extends RuntimeException> then) {
            cases.add(new Case(when, __ -> cleanAndThrow(then.get())));
            return Matching.this;
        }
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private final class Case {
        private final Predicate<? super T> p;
        private final Function<? super T, ? extends U> q;
    }
}
