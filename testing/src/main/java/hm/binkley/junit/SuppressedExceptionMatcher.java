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

package hm.binkley.junit;

import org.hamcrest.Description;
import org.hamcrest.DiagnosingMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.util.Iterator;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

/**
 * {@code SuppressedExceptionMatcher} is a Hamcrest matcher for
 * <em>suppressed</em> exceptions.  Example: Given an exception created with:
 * <pre>
 * Exception e = new SomeException();
 * e.addSuppressed(new AnotherException());
 * e.addSuppressed(new YetAnotherException("Say something"));</pre> Can be
 * matched with: <pre>
 * hasSuppressed(
 *         instanceOf(AnotherException.class),
 *         allOf(instanceOf(YetAnotherException.class),
 *                 hasMessage(equalTo("Say something"))))</pre>
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public final class SuppressedExceptionMatcher
        extends DiagnosingMatcher<Throwable> {
    private final List<Matcher<? super Throwable>> matchers;

    /**
     * Creates a new suppressed exceptions matcher for the given list of
     * <var>matchers</var>.  Each matcher corresponds to one suppressed
     * exception, in the same order.
     *
     * @param matchers the matchers, never missing
     *
     * @return the suppressed exceptions matcher, never missing
     */
    public static SuppressedExceptionMatcher hasSuppressed(
            final List<Matcher<? super Throwable>> matchers) {
        return new SuppressedExceptionMatcher(matchers);
    }

    /**
     * Creates a new suppressed exceptions matcher for the given
     * <var>matchers</var>.  Each matcher corresponds to one suppressed
     * exception, in the same order.
     *
     * @param matchers the matchers
     *
     * @return the suppressed exceptions matcher, never missing
     *
     * @see #hasSuppressed(List)
     */
    @SafeVarargs
    public static SuppressedExceptionMatcher hasSuppressed(
            final Matcher<? super Throwable>... matchers) {
        return new SuppressedExceptionMatcher(asList(matchers));
    }

    /**
     * Creates a new suppressed exceptions matcher for the given
     * <var>exceptions</var>.  Each exception is matched to one suppressed
     * exception, in the same order.
     *
     * @param exceptions the expected exceptions
     *
     * @return the suppressed exceptions matcher, never missing
     *
     * @see #hasSuppressed(List)
     */
    public static SuppressedExceptionMatcher hasSuppressed(
            final Throwable... exceptions) {
        return new SuppressedExceptionMatcher(asList(exceptions).stream().
                map(Matchers::sameInstance).
                collect(toList()));
    }

    private SuppressedExceptionMatcher(
            final List<Matcher<? super Throwable>> matchers) {
        this.matchers = matchers;
    }

    @Override
    protected boolean matches(final Object item, final Description mismatch) {
        final Throwable t = (Throwable) item;
        final List<Throwable> suppressed = asList(t.getSuppressed());
        final Iterator<Throwable> sit = suppressed.iterator();
        final Iterator<Matcher<? super Throwable>> mit = matchers.iterator();

        while (sit.hasNext() && mit.hasNext()) {
            final Throwable s = sit.next();
            final Matcher<? super Throwable> m = mit.next();
            if (!m.matches(s)) {
                mismatch.
                        appendDescriptionOf(m).
                        appendText(" ");
                m.describeMismatch(item, mismatch);
                return false;
            }
        }

        if (sit.hasNext() || mit.hasNext()) {
            mismatch.
                    appendText("has wrong suppressed exception count ").
                    appendValue(matchers.size()).
                    appendText(" expected, ").
                    appendValue(suppressed.size()).
                    appendText(" actual");
            return false;
        }

        return true;
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("hasSuppressed(");
        for (int i = 0, x = matchers.size() - 1; i < x; ++i)
            description.
                    appendDescriptionOf(matchers.get(i)).
                    appendText(" and ");
        description.
                appendDescriptionOf(matchers.get(matchers.size() - 1)).
                appendText(")");
    }
}
