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
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

import static hm.binkley.util.function.ThrowingBinaryOperator.maxBy;
import static hm.binkley.util.stream.CheckedStream.checked;
import static java.lang.System.out;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

/**
 * {@code CheckedStreamTest} tests {@link CheckedStream}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public class CheckedStreamTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

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
