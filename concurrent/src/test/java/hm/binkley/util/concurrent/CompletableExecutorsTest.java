/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org/>.
 */

package hm.binkley.util.concurrent;

import hm.binkley.util.concurrent.CompletableExecutors
        .CompletableExecutorService;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.DisableOnDebug;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static hm.binkley.util.concurrent.CompletableExecutors.completable;
import static hm.binkley.util.concurrent.CompletableExecutors
        .unwrappedCompletable;
import static java.util.Arrays.asList;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@code CompleteableExecutorsTest} tests {@link CompletableFuture}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
@RunWith(Parameterized.class)
public final class CompletableExecutorsTest {
    @Rule
    public final TestRule timeout = new DisableOnDebug(Timeout.builder().
            withTimeout(1, SECONDS).
            withLookingForStuckThread(true).
            build());
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Parameter(0)
    public String name;
    @Parameter(1)
    public Supplier<CompletableExecutorService> ctor;
    @Parameter(2)
    public Consumer<ExpectedException> whenInterrupted;

    private CompletableExecutorService threads;

    @Parameters(name = "{0}")
    public static Iterable<Object[]> parameters() {
        final Object[] jdk = args("JDK",
                () -> completable(newSingleThreadExecutor()), t -> {
                    t.expect(ExecutionException.class);
                    t.expectCause(is(instanceOf(InterruptedException.class)));
                });
        final Object[] unwrapped = args("Unwrapped",
                () -> unwrappedCompletable(newSingleThreadExecutor()),
                t -> t.expect(InterruptedException.class));

        return asList(jdk, unwrapped);
    }

    @Before
    public void setUp() {
        threads = ctor.get();
    }

    @After
    public void tearDown() {
        threads.shutdownNow();
    }

    @Test
    public void shouldGetNormally()
            throws ExecutionException, InterruptedException {
        assertThat(threads.submit(() -> 3).get(), is(equalTo(3)));
    }

    @Test
    public void shouldJoinNormally() {
        assertThat(threads.submit(() -> 3).join(), is(equalTo(3)));
    }

    @Test
    public void shouldGetExceptionally()
            throws ExecutionException, InterruptedException {
        thrown.expect(ExecutionException.class);
        thrown.expectCause(is(instanceOf(Foobar.class)));

        threads.submit(() -> {throw new Foobar();}).get();
    }

    @Test
    public void shouldJoinExceptionally() {
        thrown.expect(CompletionException.class);
        thrown.expectCause(is(instanceOf(Foobar.class)));

        threads.submit(() -> {throw new Foobar();}).join();
    }

    @Test
    public void shouldCancelForGet()
            throws InterruptedException, ExecutionException {
        thrown.expect(CancellationException.class);

        final CompletableFuture<Object> future = threads.submit(() -> {
            pause();
            return null;
        });
        future.cancel(true);
        future.get();
    }

    @Test
    public void shouldCancelForJoin() {
        thrown.expect(CancellationException.class);

        final CompletableFuture<Object> future = threads.submit(() -> {
            pause();
            return null;
        });
        future.cancel(true);
        future.join();
    }

    @Test
    public void shouldInterruptGetExternally()
            throws InterruptedException, ExecutionException {
        whenInterrupted.accept(thrown);

        final CompletableFuture<Object> future = threads.submit(() -> {
            pause();
            return null;
        });
        threads.shutdownNow();
        future.get();
    }

    @Test
    public void shouldInterruptTimedGetExternally()
            throws InterruptedException, ExecutionException, TimeoutException {
        whenInterrupted.accept(thrown);

        final CompletableFuture<Object> future = threads.submit(() -> {
            pause();
            return null;
        });
        threads.shutdownNow();
        future.get(1, SECONDS);
    }

    @Test
    public void shouldInterruptJoinExternally() {
        thrown.expect(CompletionException.class);
        thrown.expectCause(is(instanceOf(InterruptedException.class)));

        final CompletableFuture<Object> future = threads.submit(() -> {
            pause();
            return null;
        });
        threads.shutdownNow();
        future.join();
    }

    @Test
    public void shouldInterruptGetInternally()
            throws InterruptedException, ExecutionException {
        whenInterrupted.accept(thrown);

        final CompletableFuture<Object> future = threads.submit(() -> {
            throw new InterruptedException();
        });
        future.get();
    }

    @Test
    public void shouldInterruptTimedGetInternally()
            throws InterruptedException, ExecutionException, TimeoutException {
        whenInterrupted.accept(thrown);

        final CompletableFuture<Object> future = threads.submit(() -> {
            throw new InterruptedException();
        });
        future.get(1, SECONDS);
    }

    @Test
    public void shouldInterruptJoinInternally() {
        thrown.expect(CompletionException.class);
        thrown.expectCause(is(instanceOf(InterruptedException.class)));

        final CompletableFuture<Object> future = threads.submit(() -> {
            throw new InterruptedException();
        });
        future.join();
    }

    @Test
    public void shouldTimeout()
            throws InterruptedException, ExecutionException, TimeoutException {
        thrown.expect(TimeoutException.class);

        final CompletableFuture<Object> future = threads.submit(() -> {
            pause();
            return null;
        });
        future.get(1, MILLISECONDS);
    }

    @Test
    public void shouldClose()
            throws InterruptedException {
        threads.close();
        threads.awaitTermination(1, SECONDS);
    }

    private static void pause()
            throws InterruptedException {
        MILLISECONDS.sleep(100);
    }

    private static Object[] args(final String name,
            final Supplier<CompletableExecutorService> threads,
            final Consumer<ExpectedException> whenInterrupted) {
        return new Object[]{name, threads, whenInterrupted};
    }

    public static final class Foobar
            extends Exception {}
}
