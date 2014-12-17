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
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static hm.binkley.util.concurrent.CompletableExecutors.completable;
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
public class CompletableExecutorsTest {
    @Rule
    public final Timeout timeout = Timeout.builder().
            withTimeout(1, SECONDS).
            withLookingForStuckThread(true).
            build();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private CompletableExecutorService threads;

    @Before
    public void setUp() {
        threads = completable(newSingleThreadExecutor());
    }

    @Test
    public void shouldCompleteNormally()
            throws ExecutionException, InterruptedException {
        assertThat(threads.submit(() -> 3).get(), is(equalTo(3)));
    }

    @Test
    public void shouldCompleteExceptionally()
            throws ExecutionException, InterruptedException {
        thrown.expect(ExecutionException.class);
        thrown.expectCause(is(instanceOf(Foobar.class)));

        assertThat(threads.
                        submit(() -> {throw new Foobar();}).get(),
                is(equalTo(3)));
    }

    @Test
    public void shouldCancel()
            throws InterruptedException, ExecutionException {
        thrown.expect(CancellationException.class);

        final CompletableFuture<Object> future = threads.submit(() -> {
            MILLISECONDS.sleep(100);
            return null;
        });
        future.cancel(true);
        future.get();
    }

    @Test
    @Ignore("Completeable future stuffs interrupt into execution exception "
            + "for get, completion exception for join")
    public void shouldInterrupt()
            throws InterruptedException, ExecutionException {
        thrown.expect(InterruptedException.class);

        final CompletableFuture<Object> future = threads.submit(() -> {
            throw new InterruptedException();
        });
        MILLISECONDS.sleep(100);
        future.get();
    }

    public static final class Foobar
            extends Exception {}
}
