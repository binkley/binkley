package hm.binkley.util;

import org.intellij.lang.annotations.PrintFormat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.joining;

/**
 * {@code Notices} is one possible implementation for Martin Fowler's
 * suggestion, <a href="http://martinfowler.com/articles/replaceThrowWithNotification.html"><cite>Replacing
 * Throwing Exceptions with Notification in Validations</cite></a>.
 * <p>
 * Takes great care to adjust stack traces, pinpointing: <ol><li>The place were
 * a notice was added</li> <li>The place an exception was thrown if added as a
 * notice</li> <li>The place where notices were checked for</li></ol>
 * <p>
 * {@link #add(String, Object...) Text notices} are added as exception of
 * type <var>&lt;E&gt;</var>.  {@link #add(Exception) Exception notices} are
 * added as themselves (preserving type).  In both cases stack traces are
 * adjusted.
 * <p>
 * Produces a single, top-level exception of type <var>&lt;E&gt;</var> with
 * each notice exception as <em>suppressed</em> exception.  This permits code to
 * {@link #proceedOrFail() check or fail}, {@link #returnOrFail(Object)
 * return a value or fail} or {@link #returnOrFail(Supplier) compute and
 * return a value or fail}, in all cases thrown a single, top-level exception
 * summarizing notices.
 *
 * @param <E> the top-level exception type for notices
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @see <a href="http://binkley.blogspot.com/2014/12/java-validation.html"
 * title="Java validation">An earlier version</a>
 * @todo I18N for summary
 */
public final class Notices<E extends Exception>
        implements Iterable<Exception> {
    private final List<Exception> notices;
    private final BiFunction<String, Throwable, E> ctor;

    /**
     * Creates an empty set of notices based on {@code RuntimeException}.  Thus
     * checking for notices thrown an <em>unchecked</em> exception if there are
     * any, requiring no exception handling.
     *
     * @return the empty notices, never missing
     *
     * @see #noticesAs(BiFunction)
     */
    @Nonnull
    public static Notices<RuntimeException> notices() {
        return noticesAs(RuntimeException::new);
    }

    /**
     * Creates an empty set of notices based on exceptions with the given
     * 2-argument <var>ctor</var>.
     *
     * @param ctor the exception 2-argument constructor, never missing
     * @param <E> the exception type for notices
     *
     * @return the empty notices, never missing
     *
     * @see #notices()
     */
    @Nonnull
    public static <E extends Exception> Notices<E> noticesAs(
            @Nonnull final BiFunction<String, Throwable, E> ctor) {
        return new Notices<>(new ArrayList<>(0), ctor);
    }

    private Notices(final List<Exception> notices,
            final BiFunction<String, Throwable, E> ctor) {
        this.notices = notices;
        this.ctor = ctor;
    }

    /**
     * Converts these notices into a new set with a different exception type.
     * Existing text notices retain the original exception type; new text
     * notices and the top-level exception have the new excetpion type.
     *
     * @param ctor the new exception 2-argument constructor, never missing
     * @param <F> the new exception type for notices
     *
     * @return the same notices throwing a different exception, never missing
     */
    @Nonnull
    public <F extends Exception> Notices<F> as(
            @Nonnull final BiFunction<String, Throwable, F> ctor) {
        return new Notices<>(new ArrayList<>(notices), ctor);
    }

    /**
     * Checks if there are no notices.
     *
     * @return {@code true} if there are no notices
     */
    public boolean isEmpty() {
        return notices.isEmpty();
    }

    /**
     * Gets the count of notices.
     *
     * @return the count of notices
     */
    public int size() {
        return notices.size();
    }

    /**
     * An unmodifiable iterator of notices in the same order they were added.
     *
     * @return the notices iterator, never missing
     */
    @Nonnull
    @Override
    public Iterator<Exception> iterator() {
        return unmodifiableList(notices).iterator();
    }

    /**
     * Adds a new text notice, optionally formatting <var>reason</var> with
     * <var>args</var>.  Fixes the exception for this notice to show the caller
     * at the top of the stack.
     *
     * @param reason the notice reason, never missing
     * @param args formatting args to <var>reason</var>, if any
     */
    public void add(@Nonnull @PrintFormat final String reason,
            final Object... args) {
        final E cause = ctor.apply(format(reason, args), null);
        discard(cause, 2); // 2 is the magic number: lambda, current
        notices.add(cause);
    }

    /**
     * Adds a new exception notice for the given <var>cause</var>.  Fixes the
     * exception for this notice to show the caller at the top of the stack,
     * followed by existing frames in <var>cause</var>.
     *
     * @param cause the exeption to note, never missing
     */
    public void add(@Nonnull final Exception cause) {
        enhance(cause, 2, 1, currentThread().getStackTrace());
        notices.add(cause);
    }

    /**
     * Throws a top-level exception if there are notices, else does nothing.
     * Fixes the top-level exception to show the caller at the top of the
     * stack.
     *
     * @throws E if there are notices
     */
    public void proceedOrFail()
            throws E {
        if (isEmpty())
            return;
        throw fail();
    }

    /**
     * Throws a top-level exception if there are notices, else returns
     * <var>value</var>.  Fixes the top-level exception to show the caller at
     * the top of the stack.
     *
     * @param value the value to return if there are no notices
     * @param <T> the value type
     *
     * @return <va>value</var> if there are no notices
     *
     * @throws E if there are notices
     */
    public <T> T returnOrFail(@Nullable final T value)
            throws E {
        if (isEmpty())
            return value;
        throw fail();
    }

    /**
     * Throws a top-level exception if there are notices, else computes and
     * returns <var>value</var>.  Fixes the top-level exception to show the
     * caller at the top of the stack.
     *
     * @param value the value to compute and return if there are no notices
     * @param <T> the value type
     *
     * @return computation of <va>value</var> if there are no notices
     *
     * @throws E if there are notices
     */
    public <T> T returnOrFail(final Supplier<T> value)
            throws E {
        if (isEmpty())
            return value.get();
        throw fail();
    }

    /**
     * Creates a multi-line summary of notices, also used as the top-level
     * exception message.
     *
     * @return a summary of notices
     */
    @Nonnull
    public String summary() {
        if (notices.isEmpty())
            return "0 notice(s)";
        final String sep = lineSeparator() + "- ";
        return notices.stream().
                map(Throwable::getMessage).
                filter(Objects::nonNull).
                collect(joining(sep,
                        format("%d notice(s):" + sep, notices.size()), ""));
    }

    @Nonnull
    @Override
    public String toString() {
        return super.toString() + ": " + notices; // TODO: How to show E?
    }

    private E fail() {
        final E e = ctor.apply(summary(), null);
        discard(e, 3); // 3 is the magic number: lambda, outer, current
        notices.forEach(e::addSuppressed);
        return e;
    }

    private static void discard(final Exception cause, final int n) {
        final List<StackTraceElement> frames = asList(cause.getStackTrace());
        cause.setStackTrace(frames.subList(n, frames.size())
                .toArray(new StackTraceElement[frames.size() - n]));
    }

    private static void enhance(final Exception cause, final int off,
            final int n, final StackTraceElement... extras) {
        final List<StackTraceElement> frames = new ArrayList<>(
                asList(cause.getStackTrace()));
        frames.addAll(0, asList(extras).subList(off, off + n));
        cause.setStackTrace(
                frames.toArray(new StackTraceElement[frames.size()]));
    }
}
