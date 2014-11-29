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

import org.junit.Test;

import static hm.binkley.util.StackTraceFocuser.ignoreClassName;
import static hm.binkley.util.StackTraceFocuser.ignoreFileName;
import static hm.binkley.util.StackTraceFocuser.ignoreJavaClasses;
import static hm.binkley.util.StackTraceFocuser.ignoreLineNumber;
import static hm.binkley.util.StackTraceFocuser.ignoreMethodName;
import static java.util.regex.Pattern.compile;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@code StackTraceFocuserTest} tests {@link StackTraceFocuser}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public class StackTraceFocuserTest {
    @Test
    public void shouldIgnoreJavaClasses() {
        final Throwable x = new Throwable();
        final StackTraceElement nonJava = new StackTraceElement("lotro.Bilbo", "smokes",
                "Bilbo.java", 5);
        x.setStackTrace(new StackTraceElement[]{
                new StackTraceElement("java.lang.Frodo", "lives", "Frodo.java", 3), nonJava});

        assertFramesRemaining(x, ignoreJavaClasses(), nonJava);
    }

    @Test
    public void shouldIgnoreJavaxClasses() {
        final Throwable x = new Throwable();
        final StackTraceElement nonJavax = new StackTraceElement("lotro.Bilbo", "smokes",
                "Bilbo.java", 5);
        x.setStackTrace(new StackTraceElement[]{
                new StackTraceElement("javax.lang.Frodo", "lives", "Frodo.java", 3), nonJavax});

        assertFramesRemaining(x, ignoreJavaClasses(), nonJavax);
    }

    @Test
    public void shouldIgnoreSunClasses() {
        final Throwable x = new Throwable();
        final StackTraceElement nonSun = new StackTraceElement("lotro.Bilbo", "smokes",
                "Bilbo.java", 5);
        x.setStackTrace(new StackTraceElement[]{
                new StackTraceElement("sun.Frodo", "lives", "Frodo.java", 3), nonSun});

        assertFramesRemaining(x, ignoreJavaClasses(), nonSun);
    }

    @Test
    public void shouldIgnoreComSunClasses() {
        final Throwable x = new Throwable();
        final StackTraceElement nonComSun = new StackTraceElement("lotro.Bilbo", "smokes",
                "Bilbo.java", 5);
        x.setStackTrace(new StackTraceElement[]{
                new StackTraceElement("com.sun.Frodo", "lives", "Frodo.java", 3), nonComSun});

        assertFramesRemaining(x, ignoreJavaClasses(), nonComSun);
    }

    @Test
    public void shouldIgnoreWithCustomClassName() {
        final Throwable x = new Throwable();
        final StackTraceElement nonWindfola = new StackTraceElement("lotro.Bilbo", "smokes",
                "Bilbo.java", 5);
        x.setStackTrace(new StackTraceElement[]{
                new StackTraceElement("windfola.Frodo", "lives", "Frodo.java", 3), nonWindfola});

        assertFramesRemaining(x, new StackTraceFocuser<>(ignoreClassName(compile("^windfola\\."))),
                nonWindfola);
    }

    @Test
    public void shouldIgnoreWithCustomMethodName() {
        final Throwable x = new Throwable();
        final StackTraceElement nonLives = new StackTraceElement("lotro.Bilbo", "smokes",
                "Bilbo.java", 5);
        x.setStackTrace(new StackTraceElement[]{
                new StackTraceElement("windfola.Frodo", "lives", "Frodo.java", 3), nonLives});

        assertFramesRemaining(x, new StackTraceFocuser<>(ignoreMethodName(compile("lives"))),
                nonLives);
    }

    @Test
    public void shouldIgnoreWithCustomFileName() {
        final Throwable x = new Throwable();
        final StackTraceElement nonFrodo = new StackTraceElement("lotro.Bilbo", "smokes",
                "Bilbo.java", 5);
        x.setStackTrace(new StackTraceElement[]{
                new StackTraceElement("windfola.Frodo", "lives", "Frodo.java", 3), nonFrodo});

        assertFramesRemaining(x, new StackTraceFocuser<>(ignoreFileName(compile("Frodo\\.java"))),
                nonFrodo);
    }

    @Test
    public void shouldIgnoreWithCustomLineNumber() {
        final Throwable x = new Throwable();
        final StackTraceElement nonThree = new StackTraceElement("lotro.Bilbo", "smokes",
                "Bilbo.java", 5);
        x.setStackTrace(new StackTraceElement[]{
                new StackTraceElement("windfola.Frodo", "lives", "Frodo.java", 3), nonThree});

        assertFramesRemaining(x, new StackTraceFocuser<>(ignoreLineNumber(compile("3"))), nonThree);
    }

    @Test
    public void shouldIgnoreCombinedFilters() {
        final Throwable x = new Throwable();
        final StackTraceElement nonFiltered = new StackTraceElement("lotro.Bilbo", "smokes",
                "Bilbo.java", 5);
        x.setStackTrace(new StackTraceElement[]{
                new StackTraceElement("windfola.Frodo", "lives", "Frodo.java", 3),
                new StackTraceElement("windfola.Sam", "cooks", "Sam.java", 11), nonFiltered});

        assertFramesRemaining(x, new StackTraceFocuser<>(ignoreLineNumber(compile("3")),
                ignoreFileName(compile("Sam"))), nonFiltered);
    }

    private static void assertFramesRemaining(final Throwable x,
            final StackTraceFocuser<Throwable> focuser, final StackTraceElement... remaining) {
        assertThat(focuser.apply(x).getStackTrace(), is(arrayContaining(remaining)));
    }
}
