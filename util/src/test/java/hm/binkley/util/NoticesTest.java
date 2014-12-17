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

package hm.binkley.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static hm.binkley.junit.SuppressedExceptionMatcher.hasSuppressed;
import static hm.binkley.util.Notices.notices;
import static java.lang.Thread.currentThread;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.ThrowableMessageMatcher.hasMessage;

/**
 * {@code NoticesTest} tests {@link Notices}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Use of junit internal API - why no public exception matchers?
 */
public final class NoticesTest {
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldStartEmpty() {
        assertThat(notices().isEmpty(), is(true));
    }

    @Test
    public void shouldHaveSize0WhenEmpty() {
        assertThat(notices().size(), is(0));
    }

    @Test
    public void shouldSummarizeWhenEmpty() {
        assertThat(notices().summary(), containsString("0 notice(s)"));
    }

    @Test
    public void shouldProceedWhenEmpty() {
        notices().proceedOrFail();
    }

    @Test
    public void shouldReturnWhenEmpty() {
        assertThat(notices().returnOrFail(3), is(equalTo(3)));
    }

    @Test
    public void shouldComputeWhenEmpty() {
        assertThat(notices().returnOrFail(() -> 3), is(equalTo(3)));
    }

    @Test
    public void shouldRecordPlainTextNotices() {
        final Notices<RuntimeException> notices = notices();
        notices.add("Hi, mom!");

        assertThat(notices.size(), is(1));
    }

    @Test
    public void shouldRecordExceptionNotices() {
        final Notices<RuntimeException> notices = notices();
        notices.add(new Foobar());

        assertThat(notices.size(), is(1));
    }

    @Test
    public void shouldThrowPlainTextNotices() {
        final Notices<RuntimeException> notices = notices();
        notices.add("Hi, mom!");

        thrown.expect(RuntimeException.class);
        thrown.expectMessage(allOf(containsString("1 notice(s)"),
                containsString("Hi, mom!")));
        thrown.expect(hasSuppressed(instanceOf(RuntimeException.class)));
        thrown.expect(hasSuppressed(
                allOf(is(instanceOf(RuntimeException.class)),
                        hasMessage(equalTo("Hi, mom!")))));

        notices.proceedOrFail();
    }

    @Test
    public void shouldThrowExceptionNotices() {
        final Notices<RuntimeException> notices = notices();
        final Foobar cause = new Foobar();
        notices.add(cause);

        thrown.expect(RuntimeException.class);
        thrown.expectMessage(containsString("1 notice(s)"));
        thrown.expect(hasSuppressed(cause));

        notices.proceedOrFail();
    }

    @Test
    public void shouldFormatReason() {
        final Notices<RuntimeException> notices = notices();
        notices.add("A %s is %d", "bear", 3);

        assertThat(notices.iterator().next().getMessage(),
                is(equalTo("A bear is 3")));
    }

    @Test
    public void shouldFixStackTraceForTopLevelException() {
        final Notices<RuntimeException> notices = notices();
        notices.add("x");

        int lineNumber = 0;
        try {
            // TODO: The next two lines *must* be one after the other, no space
            lineNumber = currentThread().getStackTrace()[1].getLineNumber();
            notices.proceedOrFail();
        } catch (final RuntimeException e) {
            assertThat(e.getStackTrace()[0].getLineNumber(),
                    is(equalTo(lineNumber + 1)));
        }
    }

    @Test
    public void shouldFixStackForTextNotices() {
        final int lineNumber;
        final Notices<RuntimeException> notices = notices();
        notices.add("%d", lineNumber = currentThread().getStackTrace()[1]
                .getLineNumber());

        try {
            notices.proceedOrFail();
        } catch (final RuntimeException e) {
            assertThat(e.getSuppressed()[0].getStackTrace()[0].getLineNumber(),
                    is(equalTo(lineNumber)));
        }
    }

    @Test
    public void shouldFixStackForExceptionNotices() {
        final int lineNumber;
        final Notices<RuntimeException> notices = notices();
        notices.add(new Foobar(lineNumber = currentThread().getStackTrace()[1]
                .getLineNumber()));

        try {
            notices.proceedOrFail();
        } catch (final RuntimeException e) {
            assertThat(e.getSuppressed()[0].getStackTrace()[0].getLineNumber(),
                    is(equalTo(lineNumber)));
        }
    }

    private static final class Foobar
            extends Exception {
        Foobar() {}

        Foobar(final int ignored) {}
    }
}
