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

import org.hamcrest.CustomTypeSafeMatcher;

import java.util.function.Predicate;

/**
 * {@code PredicateMatcher} applies a Java 8 {@link Predicate} for assertion
 * checking.  This supports complex transformations of test objects via Java 8
 * lambdas.  Example: <pre>Collection&lt;URI&gt; uris = ...;
 * assertThat(uris, tests("location ends with 'boxley'",
 *         u -> u.stream().
 *         map(URI::getPath).
 *         filter(p -> p.endsWith("boxley")).
 *         findFirat().
 *         isPresent()));</pre> Tests that at least one of a collection of
 * URIs has a path component ending with the text, "boxley".
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public final class PredicateMatcher<T>
        extends CustomTypeSafeMatcher<T> {
    private final Predicate<T> test;

    /**
     * Creates a new {@code PredicateMatcher} for the given
     * <var>description</var> and <var>test</var>.
     *
     * @param description the Hamcrest description, never missing
     * @param test the Java 8 predicate, never missing
     * @param <T> the item type to examine
     *
     * @return the new matcher, never missing
     */
    public static <T> PredicateMatcher<T> tests(final String description,
            final Predicate<T> test) {
        return new PredicateMatcher<>(description, test);
    }

    private PredicateMatcher(final String description,
            final Predicate<T> test) {
        super(description);
        this.test = test;
    }

    @Override
    protected boolean matchesSafely(final T item) {
        return test.test(item);
    }
}
