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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static hm.binkley.util.Mixin.Factory.newMixin;
import static java.util.concurrent.Executors.callable;

/**
 * {@code CompleteableExecutors} are executors returning {@link
 * CompletableFuture} rather than plain {@link Future}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 * @todo Think through completable for scheduled
 */
public final class CompletableExecutors {
    public static CompletableExecutorService completable(
            @Nonnull final ExecutorService threads) {
        return newMixin(CompletableExecutorService.class,
                new Completable(threads), threads);
    }

    public interface CompletableExecutorService
            extends ExecutorService {
        @Nonnull
        @Override
        <T> CompletableFuture<T> submit(@Nonnull final Callable<T> task);

        @Nonnull
        @Override
        <T> CompletableFuture<T> submit(@Nonnull final Runnable task,
                @Nullable final T result);

        @Nonnull
        @Override
        CompletableFuture<?> submit(@Nonnull final Runnable task);
    }

    public static final class Completable {
        private final ExecutorService threads;

        private Completable(final ExecutorService threads) {
            this.threads = threads;
        }

        @Nonnull
        public <T> CompletableFuture<T> submit(
                @Nonnull final Callable<T> task) {
            final CompletableFuture<T> cf = new CompletableFuture<>();
            threads.submit(() -> {
                try {
                    cf.complete(task.call());
                } catch (final Exception e) {
                    cf.completeExceptionally(e);
                }
            });
            return cf;
        }

        @Nonnull
        public <T> CompletableFuture<T> submit(@Nonnull final Runnable task,
                @Nullable final T result) {
            return submit(callable(task, result));
        }

        @Nonnull
        public CompletableFuture<?> submit(@Nonnull final Runnable task) {
            return submit(callable(task));
        }
    }
}
