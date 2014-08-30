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

package hm.binkley.util.stream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.nio.file.AccessDeniedException;
import java.security.AccessControlException;
import java.util.Iterator;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.stream.Stream;

import static hm.binkley.util.function.ThrowingBinaryOperator.maxBy;
import static hm.binkley.util.function.ThrowingPredicate.isEqual;
import static hm.binkley.util.stream.CheckedStream.checked;
import static java.lang.System.out;
import static java.lang.Thread.currentThread;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * {@code CheckedStreamTest} tests {@link CheckedStream}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public class CheckedStreamTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldIterate()
            throws InterruptedException {
        final Iterator<Integer> it = checked(Stream.of(1)).iterator();
        assertThat(it.hasNext(), is(true));
        assertThat(it.next(), is(equalTo(1)));
        assertThat(it.hasNext(), is(false));
    }

    @Test
    public void shouldRunInThisThread()
            throws InterruptedException {
        checked(Stream.of(1)).
                map(i -> currentThread()).
                forEach(t -> assertThat(t, is(sameInstance(currentThread()))));
    }

    @Test
    public void shouldRunInPoolThread()
            throws InterruptedException {
        final ForkJoinPool threads = new ForkJoinPool(2, pool -> new ForkJoinWorkerThread(pool) {{
            setName("Foo!");
        }}, null, true);

        checked(Stream.of(1), threads).
                map(i -> currentThread()).
                forEach(t -> assertThat(t.getName(), is(equalTo("Foo!"))));
    }

    @Test
    public void shouldTerminateForAnyMatchWhenSequential()
            throws InterruptedException {
        assertThat(checked(Stream.of(1, 2, 3)).
                anyMatch(isEqual(1)), is(true));
    }

    @Test
    public void shouldTerminateForAllMatchWhenSequential()
            throws InterruptedException {
        assertThat(checked(Stream.of(1, 2, 3)).
                anyMatch(i -> true), is(true));
    }

    @Test
    public void shouldTerminateForNoneMatchWhenSequential()
            throws InterruptedException {
        assertThat(checked(Stream.of(1, 2, 3)).
                noneMatch(i -> true), is(true));
    }

    @Test
    public void shouldTerminateForAnyMatchWhenParallel()
            throws InterruptedException {
        assertThat(checked(Stream.of(1, 2, 3), new ForkJoinPool()).
                anyMatch(isEqual(1)), is(true));
    }

    @Test
    public void shouldTerminateForAllMatchWhenParallel()
            throws InterruptedException {
        assertThat(checked(Stream.of(1, 2, 3), new ForkJoinPool()).
                anyMatch(i -> true), is(true));
    }

    @Test
    public void shouldTerminateForNoneMatchWhenParallel()
            throws InterruptedException {
        assertThat(checked(Stream.of(1, 2, 3), new ForkJoinPool()).
                noneMatch(i -> true), is(true));
    }

    @Test
    public void shouldThrowCheckedWhenSequential()
            throws AccessDeniedException, InterruptedException {
        thrown.expect(AccessDeniedException.class);
        thrown.expectMessage("Foo!");

        checked(Stream.of(1, 2, 3)).
                filter(i -> {
                    throw new AccessDeniedException("Foo!");
                }).
                count();
    }

    @Test
    public void shouldThrowUncheckedWhenSequential()
            throws InterruptedException {
        thrown.expect(AccessControlException.class);
        thrown.expectMessage("Foo!");

        checked(Stream.of(1, 2, 3)).
                filter(i -> {
                    throw new AccessControlException("Foo!");
                }).
                count();
    }

    @Test
    public void shouldThrowErrorWhenSequential()
            throws InterruptedException {
        thrown.expect(IllegalAccessError.class);
        thrown.expectMessage("Foo!");

        checked(Stream.of(1, 2, 3)).
                filter(i -> {
                    throw new IllegalAccessError("Foo!");
                }).
                count();
    }

    @Test
    public void shouldThrowCheckedWhenParallel()
            throws AccessDeniedException, InterruptedException {
        thrown.expect(AccessDeniedException.class);
        thrown.expectMessage("Foo!");

        checked(Stream.of(1, 2, 3), new ForkJoinPool()).
                filter(i -> {
                    throw new AccessDeniedException("Foo!");
                }).
                count();
    }

    @Test
    public void shouldThrowUncheckedWhenParallel()
            throws InterruptedException {
        thrown.expect(AccessControlException.class);
        thrown.expectMessage("Foo!");

        checked(Stream.of(1, 2, 3), new ForkJoinPool()).
                filter(i -> {
                    throw new AccessControlException("Foo!");
                }).
                count();
    }

    @Test
    public void shouldThrowErrorWhenParallel()
            throws InterruptedException {
        thrown.expect(IllegalAccessError.class);
        thrown.expectMessage("Foo!");

        checked(Stream.of(1, 2, 3), new ForkJoinPool()).
                filter(i -> {
                    throw new IllegalAccessError("Foo!");
                }).
                count();
    }

    @Test
    public void shouldThrowUserRuntimeExceptionWhenParallelForBoolean()
            throws InterruptedException {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Foo!");
        final RuntimeException innerCause = new RuntimeException("Bar!");
        thrown.expectCause(is(sameInstance(innerCause)));

        checked(Stream.of(1, 2, 3), new ForkJoinPool()).
                filter(i -> {
                    throw new RuntimeException("Foo!", innerCause);
                }).
                anyMatch(isEqual(1));
    }

    @Test
    public void shouldThrowUserRuntimeExceptionWhenParallelForLong()
            throws InterruptedException {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Foo!");
        final RuntimeException innerCause = new RuntimeException("Bar!");
        thrown.expectCause(is(sameInstance(innerCause)));

        checked(Stream.of(1, 2, 3), new ForkJoinPool()).
                filter(i -> {
                    throw new RuntimeException("Foo!", innerCause);
                }).
                count();
    }

    @Test
    public void shouldThrowUserRuntimeExceptionWhenParallelForObject()
            throws Exception {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Foo!");
        final RuntimeException innerCause = new RuntimeException("Bar!");
        thrown.expectCause(is(sameInstance(innerCause)));

        checked(Stream.of(1, 2, 3), new ForkJoinPool()).
                filter(i -> {
                    throw new RuntimeException("Foo!", innerCause);
                }).
                // TODO: Reduce here throws Exception, not E
                        reduce(0, maxBy(Integer::compare));
    }

    @Test
    public void shouldThrowUserRuntimeExceptionWhenParallelForVoid()
            throws InterruptedException {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Foo!");
        final RuntimeException innerCause = new RuntimeException("Bar!");
        thrown.expectCause(is(sameInstance(innerCause)));

        checked(Stream.of(1, 2, 3), new ForkJoinPool()).
                filter(i -> {
                    throw new RuntimeException("Foo!", innerCause);
                }).
                forEach(out::println);
    }
}
