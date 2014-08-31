package hm.binkley.util.stream;

import hm.binkley.util.function.ThrowingBiFunction;
import hm.binkley.util.function.ThrowingBinaryOperator;
import hm.binkley.util.function.ThrowingBooleanSupplier;
import hm.binkley.util.function.ThrowingConsumer;
import hm.binkley.util.function.ThrowingFunction;
import hm.binkley.util.function.ThrowingLongSupplier;
import hm.binkley.util.function.ThrowingPredicate;
import hm.binkley.util.function.ThrowingRunnable;
import hm.binkley.util.function.ThrowingSupplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static hm.binkley.util.function.ThrowingFunction.identity;
import static hm.binkley.util.function.ThrowingPredicate.isEqual;
import static java.lang.Thread.currentThread;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

/**
 * {@code CheckedStream} is a <em>throwing</em> {@link Stream} look-a-like with control over {@link
 * #parallel(ForkJoinPool) thread pool}.  It cannot be a {@code Stream} as it takes throwing
 * versions of suppliers, functions and consumers. Otherwise it is a faithful reproduction.
 * <p>
 * Write this: <pre>
 * long beanCount() throws SomeException, OtherException {
 *     checked(Stream.of(1, 2, 3)).
 *         map(this::someThrowingFunction).
 *         peek(That::oldBean).
 *         count();
 * }
 * </pre> not this: <pre>
 * long beanCount() throws SomeException, OtherException {
 *     try {
 *         Stream.of(1, 2, 3).
 *             map(i -> {
 *                 try {
 *                     someThrowingFunction(i);
 *                 } catch (final SomeException e) {
 *                     throw new RuntimeException(e);
 *                 }
 *             }).
 *             peek(i -> {
 *                 try {
 *                     That.oldBean(i);
 *                 } catch (final OtherException e) {
 *                     throw new RuntimeException(e);
 *                 }
 *             }).
 *             count();
 *     } catch (final RuntimeException e) {
 *         final Throwable x = e.getCause();
 *         if (x instanceof SomeException)
 *             throw (SomeException) x;
 *         if (x instanceof OtherException)
 *             throw (OtherException) x;
 *         throw e;
 *     }
 * }
 * </pre>
 * "Intentional" exceptions (checked exceptions plus {@code CancellationException}) have "scrubbed"
 * stacktraces: frames from framework/glue packages are removed before the intentional exception is
 * rethrown to calling code. Scrubbed stacktraces are much easier to understand, the framework and
 * glue code having been removed.
 * <p>
 * To see the unscrubbed stacktrace, set the system property "hm.binkley.util.stream.CheckedStream.debug"
 * to "true".
 * <p>
 * Controlling the thread pool used by {@code Stream} is a challenge.  Deep in the implementation,
 * it checks if being run in a {@link ForkJoinTask}, and uses that thread if so, otherwise using the
 * {@link ForkJoinPool#commonPool() common pool}.  So with {@code CheckedStream} write this:
 * <pre>
 *     checked(stream, new ForkJoinPool()).
 *         map(currentThread()).
 *         forEach(System.out::println);
 * </pre> not this: <pre>
 *     try {
 *         new ForkJoinPool().submit(() -> stream.
 *                 map(currentThread()).
 *                 forEach(System.out::println)).
 *             get();
 *     } catch (final ExecutionException e) {
 *         final Throwable x = e.getCause();
 *         if (x instanceof Error)
 *             throw (Error) x;
 *         if (x instanceof RuntimeException)
 *             // Much tricker when stream functions throw runtime
 *             throw (RuntimeException) x;
 *         throw new Error(e); // We have no checked exceptions in this example
 *     }
 * </pre>
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Optimize non-throwing, non-terminal calls: they don't need wrapping, but keep throws E
 */
@SuppressWarnings("UnusedDeclaration")
public abstract class CheckedStream<T>
        implements AutoCloseable {
    private static final String className = CheckedStream.class.getName();
    private static final String innerName = className + "$";
    private static final String funcName = ThrowingFunction.class.getPackage().getName();
    private static final String javaName = "java.util.";
    private static final boolean debug = Boolean.getBoolean(className + ".debug");
    private final Stream<T> stream;

    /**
     * Creates a new sequential {@code CheckedStream} wrapping the given <var>stream</var> stream.
     *
     * @param stream the delegated stream, never missing
     * @param <T> the stream element type
     *
     * @return the new checked stream, never missing
     */
    @Nonnull
    public static <T> CheckedStream<T> checked(@Nonnull final Stream<T> stream) {
        return new SequentialCheckedStream<>(stream);
    }

    /**
     * Creates a new parallel {@code CheckedStream} wrapping the given <var>stream</var> stream and
     * executing on <var>threads</var>.
     *
     * @param stream the delegated stream, never missing
     * @param threads the fork-join thread pool, never missing
     * @param <T> the stream element type
     *
     * @return the new checked stream, never missing
     */
    @Nonnull
    public static <T> CheckedStream<T> checked(@Nonnull final Stream<T> stream,
            @Nonnull final ForkJoinPool threads) {
        return new ParallelCheckedStream<>(stream, threads);
    }

    /**
     * Constructs a new {@code CheckedStream} for the given <var>delegate</var> stream and
     * <var>thrown</var> exception holder.
     *
     * @param stream the delegated stream, never missing
     */
    protected CheckedStream(@Nonnull final Stream<T> stream) {
        this.stream = stream;
    }

    @Nonnull
    protected abstract <U> CheckedStream<U> next(@Nonnull final Stream<U> stream);

    protected abstract <E extends Exception> void terminateVoid(
            @Nonnull final ThrowingRunnable<E> action)
            throws E, InterruptedException;

    protected abstract <U, E extends Exception> U terminateConcrete(
            @Nonnull final ThrowingSupplier<U, E> supplier)
            throws E, InterruptedException;

    protected abstract <E extends Exception> long terminateLong(
            @Nonnull final ThrowingLongSupplier<E> supplier)
            throws E, InterruptedException;

    protected abstract <E extends Exception> boolean terminateBoolean(
            @Nonnull final ThrowingBooleanSupplier<E> supplier)
            throws E, InterruptedException;

    /** Gets the delegated stream. */
    @Nonnull
    public final Stream<T> asStream() {
        return stream;
    }

    /** @see Stream#iterator() */
    @Nonnull
    public final <E extends Exception> Iterator<T> iterator()
            throws E, InterruptedException {
        final ThrowingSupplier<Iterator<T>, E> terminal = () -> evaluateObject(stream::iterator);
        return terminateConcrete(terminal);
    }

    /** @see Stream#spliterator() */
    @Nonnull
    public final <E extends Exception> Spliterator<T> spliterator()
            throws E, InterruptedException {
        final ThrowingSupplier<Spliterator<T>, E> terminal = () -> evaluateObject(
                stream::spliterator);
        return terminateConcrete(terminal);
    }

    /** @see Stream#isParallel() */
    public final boolean isParallel() {
        return stream.isParallel();
    }

    /** @see Stream#sequential() */
    @Nonnull
    public abstract CheckedStream<T> sequential()
            throws Exception;

    /** @see Stream#parallel() */
    @Nonnull
    public abstract CheckedStream<T> parallel(@Nonnull final ForkJoinPool threads)
            throws Exception;

    /** @see Stream#unordered() */
    @Nonnull
    public final <E extends Exception> CheckedStream<T> unordered()
            throws InterruptedException {
        return evaluateStream(stream::unordered);
    }

    /** @see Stream#onClose(Runnable) */
    @Nonnull
    public final CheckedStream<T> onClose(@Nonnull final ThrowingRunnable<?> closeHandler) {
        return next(stream.onClose(closeHandler.asRunnable(StreamException::new)));
    }

    /** @see Stream#filter(Predicate) */
    @Nonnull
    public final <E extends Exception> CheckedStream<T> filter(
            @Nonnull final ThrowingPredicate<? super T, E> predicate)
            throws E, InterruptedException {
        return evaluateStream(() -> stream.filter(predicate.asPredicate(StreamException::new)));
    }

    /** @see Stream#map(Function) */
    @Nonnull
    public final <R, E extends Exception> CheckedStream<R> map(
            @Nonnull final ThrowingFunction<? super T, ? extends R, E> mapper)
            throws E, InterruptedException {
        return evaluateStream(() -> stream.map(mapper.asFunction(StreamException::new)));
    }

    /**
     * @todo Throwing version
     * @see Stream#mapToInt(ToIntFunction)
     */
    @Nonnull
    public final IntStream mapToInt(@Nonnull final ToIntFunction<? super T> mapper)
            throws InterruptedException {
        return evaluateObject(() -> stream.mapToInt(mapper));
    }

    /**
     * @todo Throwing version
     * @see Stream#mapToLong(ToLongFunction)
     */
    @Nonnull
    public final LongStream mapToLong(@Nonnull final ToLongFunction<? super T> mapper)
            throws InterruptedException {
        return evaluateObject(() -> stream.mapToLong(mapper));
    }

    /**
     * @todo Throwing version
     * @see Stream#mapToDouble(ToDoubleFunction)
     */
    @Nonnull
    public final DoubleStream mapToDouble(@Nonnull final ToDoubleFunction<? super T> mapper)
            throws InterruptedException {
        return evaluateObject(() -> stream.mapToDouble(mapper));
    }

    /** @see Stream#flatMap(Function) */
    @Nonnull
    public final <R, E extends Exception> CheckedStream<R> flatMap(@Nonnull
    final ThrowingFunction<? super T, ? extends Stream<? extends R>, E> mapper)
            throws E, InterruptedException {
        return evaluateStream(() -> stream.flatMap(mapper.asFunction(StreamException::new)));
    }

    /**
     * @todo Throwing version
     * @see Stream#flatMapToInt(Function)
     */
    @Nonnull
    public final IntStream flatMapToInt(
            @Nonnull final Function<? super T, ? extends IntStream> mapper)
            throws InterruptedException {
        return evaluateObject(() -> stream.flatMapToInt(mapper));
    }

    /**
     * @todo Throwing version
     * @see Stream#flatMapToLong(Function)
     */
    @Nonnull
    public final LongStream flatMapToLong(
            @Nonnull final Function<? super T, ? extends LongStream> mapper)
            throws InterruptedException {
        return evaluateObject(() -> stream.flatMapToLong(mapper));
    }

    /**
     * @todo Throwing version
     * @see Stream#flatMapToDouble(Function)
     */
    @Nonnull
    public final DoubleStream flatMapToDouble(
            @Nonnull final Function<? super T, ? extends DoubleStream> mapper)
            throws InterruptedException {
        return evaluateObject(() -> stream.flatMapToDouble(mapper));
    }

    /** @see Stream#distinct() */
    @Nonnull
    public final CheckedStream<T> distinct()
            throws InterruptedException {
        return evaluateStream(stream::distinct);
    }

    /** @see Stream#sorted() */
    @Nonnull
    public final CheckedStream<T> sorted()
            throws InterruptedException {
        final ThrowingSupplier<CheckedStream<T>, RuntimeException> terminal = () -> evaluateStream(
                stream::sorted);
        return terminateConcrete(terminal);
    }

    /** @see Stream#sorted(Comparator) */
    @Nonnull
    public final CheckedStream<T> sorted(@Nonnull final Comparator<? super T> comparator)
            throws InterruptedException {
        final ThrowingSupplier<CheckedStream<T>, RuntimeException> terminal = () -> evaluateStream(
                () -> stream.sorted(comparator));
        return terminateConcrete(terminal);
    }

    /** @see Stream#peek(Consumer) */
    @Nonnull
    public final <E extends Exception> CheckedStream<T> peek(
            @Nonnull final ThrowingConsumer<? super T, E> action)
            throws E, InterruptedException {
        return evaluateStream(() -> stream.peek(action.asConsumer(StreamException::new)));
    }

    /** @see Stream#limit(long) */
    @Nonnull
    public final CheckedStream<T> limit(final long maxSize)
            throws InterruptedException {
        return evaluateStream(() -> stream.limit(maxSize));
    }

    /** @see Stream#skip(long) */
    @Nonnull
    public final CheckedStream<T> skip(final long n)
            throws InterruptedException {
        return evaluateStream(() -> stream.skip(n));
    }

    /** @see Stream#forEach(Consumer) */
    public final <E extends Exception> void forEach(
            @Nonnull final ThrowingConsumer<? super T, E> action)
            throws InterruptedException {
        final ThrowingRunnable<RuntimeException> terminal = () -> evaluateVoid(
                () -> stream.forEach(action.asConsumer(StreamException::new)));
        terminateVoid(terminal);
    }

    /** @see Stream#forEachOrdered(Consumer) */
    public final <E extends Exception> void forEachOrdered(
            @Nonnull final ThrowingConsumer<? super T, E> action)
            throws E, InterruptedException {
        final ThrowingRunnable<RuntimeException> terminal = () -> evaluateVoid(
                () -> stream.forEachOrdered(action.asConsumer(StreamException::new)));
        terminateVoid(terminal);
    }

    /** @see Stream#toArray() */
    @Nonnull
    public final Object[] toArray()
            throws InterruptedException {
        final ThrowingSupplier<Object[], RuntimeException> terminal = () -> evaluateObject(
                stream::toArray);
        return terminateConcrete(terminal);
    }

    /** @see Stream#toArray(IntFunction) */
    @Nonnull
    public final <A> A[] toArray(@Nonnull final IntFunction<A[]> generator)
            throws InterruptedException {
        final ThrowingSupplier<A[], RuntimeException> terminal = () -> evaluateObject(
                () -> stream.toArray(generator));
        return terminateConcrete(terminal);
    }

    /** @see Stream#reduce(Object, BiFunction, BinaryOperator) */
    public final <E extends Exception> T reduce(@Nonnull final T identity,
            @Nonnull final ThrowingBinaryOperator<T, E> accumulator)
            throws E, InterruptedException {
        final ThrowingSupplier<T, E> terminal = () -> evaluateObject(
                () -> stream.reduce(identity, accumulator.asBinaryOperator(StreamException::new)));
        return terminateConcrete(terminal);
    }

    /** @see Stream#reduce(BinaryOperator) */
    @Nonnull
    public final <E extends Exception> Optional<T> reduce(
            @Nonnull final ThrowingBinaryOperator<T, E> accumulator)
            throws E, InterruptedException {
        final ThrowingSupplier<Optional<T>, E> terminal = () -> evaluateObject(
                () -> stream.reduce(accumulator.asBinaryOperator(StreamException::new)));
        return terminateConcrete(terminal);
    }

    /** @see Stream#reduce(Object, BinaryOperator) */
    public final <U, E extends Exception> U reduce(@Nullable final U identity,
            @Nonnull final ThrowingBiFunction<U, ? super T, U, E> accumulator,
            @Nonnull final ThrowingBinaryOperator<U, E> combiner)
            throws E, InterruptedException {
        final ThrowingSupplier<U, E> terminal = () -> evaluateObject(() -> stream
                .reduce(identity, accumulator.asBiFunction(StreamException::new),
                        combiner.asBinaryOperator(StreamException::new)));
        return terminateConcrete(terminal);
    }

    /**
     * @todo Throwing version
     * @see Stream#collect(Supplier, BiConsumer, BiConsumer)
     */
    @Nonnull
    public final <R, E extends Exception> R collect(@Nonnull final Supplier<R> supplier,
            @Nonnull final BiConsumer<R, ? super T> accumulator,
            @Nonnull final BiConsumer<R, R> combiner)
            throws E, InterruptedException {
        final ThrowingSupplier<R, E> terminal = () -> evaluateObject(
                () -> stream.collect(supplier, accumulator, combiner));
        return terminateConcrete(terminal);
    }

    /**
     * @todo Throwing version
     * @see Stream#collect(Collector)
     */
    @Nonnull
    public final <R, A, E extends Exception> R collect(
            @Nonnull final Collector<? super T, A, R> collector)
            throws E, InterruptedException {
        final ThrowingSupplier<R, E> terminal = () -> evaluateObject(
                () -> stream.collect(collector));
        return terminateConcrete(terminal);
    }

    /** @see Stream#min(Comparator) */
    @Nonnull
    public final Optional<T> min(@Nonnull final Comparator<? super T> comparator)
            throws InterruptedException {
        final ThrowingSupplier<Optional<T>, RuntimeException> terminal = () -> evaluateObject(
                () -> stream.min(comparator));
        return terminateConcrete(terminal);
    }

    /** @see Stream#max(Comparator) */
    @Nonnull
    public final Optional<T> max(@Nonnull final Comparator<? super T> comparator)
            throws InterruptedException {
        final ThrowingSupplier<Optional<T>, RuntimeException> terminal = () -> evaluateObject(
                () -> stream.max(comparator));
        return terminateConcrete(terminal);
    }

    /** @see Stream#count() */
    public final long count()
            throws InterruptedException {
        return terminateLong(() -> evaluateLong(stream::count));
    }

    /** @see Stream#anyMatch(Predicate) */
    public final <E extends Exception> boolean anyMatch(
            @Nonnull final ThrowingPredicate<? super T, E> predicate)
            throws E, InterruptedException {
        final ThrowingBooleanSupplier<RuntimeException> terminal = () -> evaluateBoolean(
                () -> stream.anyMatch(predicate.asPredicate(StreamException::new)));
        return terminateBoolean(terminal);
    }

    /** @see Stream#allMatch(Predicate) */
    public final <E extends Exception> boolean allMatch(
            @Nonnull final ThrowingPredicate<? super T, E> predicate)
            throws E, InterruptedException {
        final ThrowingBooleanSupplier<RuntimeException> terminal = () -> evaluateBoolean(
                () -> stream.allMatch(predicate.asPredicate(StreamException::new)));
        return terminateBoolean(terminal);
    }

    /** @see Stream#noneMatch(Predicate) */
    public final <E extends Exception> boolean noneMatch(
            @Nonnull final ThrowingPredicate<? super T, E> predicate)
            throws E, InterruptedException {
        final ThrowingBooleanSupplier<RuntimeException> terminal = () -> evaluateBoolean(
                () -> stream.noneMatch(predicate.asPredicate(StreamException::new)));
        return terminateBoolean(terminal);
    }

    /** @see Stream#findFirst() */
    @Nonnull
    public final Optional<T> findFirst()
            throws InterruptedException {
        final ThrowingSupplier<Optional<T>, RuntimeException> terminal = () -> evaluateObject(
                stream::findFirst);
        return terminateConcrete(terminal);
    }

    /** @see Stream#findAny() */
    @Nonnull
    public final Optional<T> findAny()
            throws InterruptedException {
        final ThrowingSupplier<Optional<T>, RuntimeException> terminal = () -> evaluateObject(
                stream::findAny);
        return terminateConcrete(terminal);
    }

    /**
     * Closes the delegated stream.
     *
     * @throws Exception if any registered {@link #onClose(ThrowingRunnable) close handlers} throw
     */
    @Override
    public final void close()
            throws Exception {
        terminateVoid(() -> evaluateVoid(stream::close));
    }

    // Implementation

    /**
     * Supports "immediate" operations, those which consume the current delegated stream and create
     * a new one.  Use <var>parallel</var> to control the new stream async state.
     *
     * @param parallel {@code true} if the new stream should be parallel
     */
    protected final Stream<T> immediate(final boolean parallel)
            throws Exception {
        // Cannot use spliterator: we mix terminals mid-stream in suport of parallel/sequential, so
        // need a fresh stream for immediate operations:
        // return StreamSupport.stream(spliterator(), isParallel());
        final List<T> collected = stream.collect(toList());
        return parallel ? collected.parallelStream() : collected.stream();
    }

    private <R, E extends Exception> CheckedStream<R> evaluateStream(
            final Supplier<Stream<R>> frame)
            throws E, InterruptedException {
        try {
            return next(frame.get());
        } catch (final StreamException thrown) {
            throw thrown.<E>cast();
        }
    }

    private static <U, E extends Exception> U evaluateObject(final Supplier<U> frame)
            throws E, InterruptedException {
        try {
            return frame.get();
        } catch (final StreamException thrown) {
            throw thrown.<E>cast();
        }
    }

    private static <E extends Exception> void evaluateVoid(final Runnable frame)
            throws E, InterruptedException {
        try {
            frame.run();
        } catch (final StreamException thrown) {
            throw thrown.<E>cast();
        }
    }

    private static <E extends Exception> boolean evaluateBoolean(final BooleanSupplier supplier)
            throws E, InterruptedException {
        try {
            return supplier.getAsBoolean();
        } catch (final StreamException thrown) {
            throw thrown.<E>cast();
        }
    }

    private static <E extends Exception> long evaluateLong(final LongSupplier supplier)
            throws E, InterruptedException {
        try {
            return supplier.getAsLong();
        } catch (final StreamException thrown) {
            throw thrown.<E>cast();
        }
    }

    protected static final class StreamException
            extends RuntimeException {
        public StreamException(final Exception e) {
            super(e);
        }

        @SuppressWarnings("unchecked")
        public <E extends Exception> E cast()
                throws InterruptedException {
            final Throwable cause = getCause();
            final Throwable[] suppressed = getSuppressed();
            for (final Throwable x : suppressed)
                cause.addSuppressed(x);

            if (cause instanceof CancellationException)
                throw scrub((CancellationException) cause);
            if (cause instanceof InterruptedException) {
                currentThread().interrupt();
                throw scrub((InterruptedException) cause);
            }

            return scrub((E) cause);
        }

        /** When not debugging checked stream, removes framework/glue stack frames. */
        private static <E extends Exception> E scrub(final E e) {
            if (debug)
                return e;

            final StackTraceElement[] stack = e.getStackTrace();
            final List<StackTraceElement> scrubbed = new ArrayList<>(stack.length);
            for (final StackTraceElement element : stack) {
                final String frameName = element.getClassName();
                if (className.equals(frameName))
                    continue;
                if (frameName.startsWith(innerName))
                    continue;
                if (frameName.startsWith(funcName))
                    continue;
                if (frameName.startsWith(javaName))
                    continue;
                scrubbed.add(element);
            }
            e.setStackTrace(scrubbed.toArray(new StackTraceElement[scrubbed.size()]));

            return e;
        }
    }

    private static final class SequentialCheckedStream<T>
            extends CheckedStream<T> {
        private SequentialCheckedStream(@Nonnull final Stream<T> delegate) {
            super(delegate);
        }

        @Nonnull
        @Override
        protected <U> CheckedStream<U> next(@Nonnull final Stream<U> stream) {
            return new SequentialCheckedStream<>(stream);
        }

        @Override
        protected <E extends Exception> void terminateVoid(
                @Nonnull final ThrowingRunnable<E> action)
                throws E, InterruptedException {
            action.run();
        }

        @Override
        protected <U, E extends Exception> U terminateConcrete(
                @Nonnull final ThrowingSupplier<U, E> supplier)
                throws E, InterruptedException {
            return supplier.get();
        }

        @Override
        protected <E extends Exception> long terminateLong(
                @Nonnull final ThrowingLongSupplier<E> supplier)
                throws E, InterruptedException {
            return supplier.getAsLong();
        }

        @Override
        protected <E extends Exception> boolean terminateBoolean(
                @Nonnull final ThrowingBooleanSupplier<E> supplier)
                throws E, InterruptedException {
            return supplier.getAsBoolean();
        }

        @Nonnull
        @Override
        public CheckedStream<T> sequential()
                throws Exception {
            return this;
        }

        @Nonnull
        @Override
        public CheckedStream<T> parallel(@Nonnull final ForkJoinPool threads)
                throws Exception {
            return new ParallelCheckedStream<>(immediate(true), threads);
        }
    }

    private static final class ParallelCheckedStream<T>
            extends CheckedStream<T> {
        private final ForkJoinPool threads;

        private ParallelCheckedStream(@Nonnull final Stream<T> delegate,
                final ForkJoinPool threads) {
            super(delegate);
            this.threads = threads;
        }

        @Nonnull
        @Override
        protected <U> CheckedStream<U> next(@Nonnull final Stream<U> stream) {
            return new ParallelCheckedStream<>(stream, threads);
        }

        @Override
        protected <E extends Exception> void terminateVoid(
                @Nonnull final ThrowingRunnable<E> action)
                throws E, InterruptedException {
            try {
                threads.submit(() -> {
                    action.run();
                    return null;
                }).get();
            } catch (final ExecutionException e) {
                throw ParallelCheckedStream.<E>handleForkJoinPool(e);
            }
        }

        @Override
        protected <U, E extends Exception> U terminateConcrete(
                @Nonnull final ThrowingSupplier<U, E> supplier)
                throws E, InterruptedException {
            try {
                return threads.submit(supplier::get).get();
            } catch (final ExecutionException e) {
                throw ParallelCheckedStream.<E>handleForkJoinPool(e);
            }
        }

        @Override
        protected <E extends Exception> long terminateLong(
                @Nonnull final ThrowingLongSupplier<E> supplier)
                throws E, InterruptedException {
            try {
                return threads.submit(supplier::getAsLong).get();
            } catch (final ExecutionException e) {
                throw ParallelCheckedStream.<E>handleForkJoinPool(e);
            }
        }

        @Override
        protected <E extends Exception> boolean terminateBoolean(
                @Nonnull final ThrowingBooleanSupplier<E> supplier)
                throws E, InterruptedException {
            try {
                return threads.submit(supplier::getAsBoolean).get();
            } catch (final ExecutionException e) {
                throw ParallelCheckedStream.<E>handleForkJoinPool(e);
            }
        }

        @Nonnull
        @Override
        public CheckedStream<T> sequential()
                throws Exception {
            return new SequentialCheckedStream<>(immediate(false));
        }

        @Nonnull
        @Override
        public CheckedStream<T> parallel(@Nonnull final ForkJoinPool threads)
                throws Exception {
            return this.threads.equals(threads) ? this
                    : new ParallelCheckedStream<>(immediate(true), threads);
        }

        /**
         * Tricky, guts of FJP wrap our checked in a runtime, wraps tha in an execution, but user
         * runtime wraps nothing - check stacktrace for thrown by ForkJoinTask.  FJP also likes to
         * wrap error (why?)
         */
        @SuppressWarnings("unchecked")
        private static <E extends Exception> E handleForkJoinPool(final ExecutionException e) {
            final Throwable cause = e.getCause();
            // Bubble out error
            if (cause instanceof Error)
                throw (Error) cause;
            // Bubble out subtypes of runtime
            if (RuntimeException.class != cause.getClass())
                return (E) cause;
            // Bubble out wrapped runtimes not wrapped by FJP
            final Throwable x = cause.getCause();
            if (null == x || !cause.getStackTrace()[0].getClassName()
                    .startsWith(ForkJoinTask.class.getName()))
                throw (RuntimeException) cause;
            // Actual user exception
            return (E) x;
        }
    }

    /** Check that every construct compiles. */
    private static void compile()
            throws InterruptedException {
        checked(Stream.of(1, 2, 3)).
                distinct().
                filter(isEqual(1)).
                flatMap(i -> range(i, i).boxed()).
                limit(1).
                map(identity()).
                onClose(() -> {
                }).
                peek(i -> {
                }).
                skip(0).
                sorted().
                unordered().
                toArray();
    }
}
