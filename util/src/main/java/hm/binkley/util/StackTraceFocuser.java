package hm.binkley.util;

import com.google.common.collect.Lists;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static hm.binkley.util.LinkedIterable.over;
import static java.util.Arrays.asList;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

/**
 * {@code StackTraceFocuser} narrows exception stack traces, discarding uniteresting frames as an
 * aid to logging or debugging.
 * <p>
 * It does not modify exception messages or suppressed exceptions, only an exception and its causal
 * chain.
 *
 * @param <E> the exception type
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public final class StackTraceFocuser<E extends Throwable>
        implements Function<E, E> {
    private static final List<Pattern> defaultClassNameIgnores = asList(compile("^java\\."),
            compile("^javax\\."), compile("^sun\\."), compile("^com\\.sun\\."));
    private final Predicate<StackTraceElement> ignore;

    /**
     * Creates a new {@code StackTraceFocuser} for the given list of <var>classNameIgnores</var>
     * regexen.
     *
     * @param classNameIgnores the patterns to ignore, never missing
     * @param <E> the exception type
     *
     * @return thew new {@code StackTraceFocuser}, never missing
     */
    @Nonnull
    public static <E extends Throwable> StackTraceFocuser<E> ignoreClassNames(
            @Nonnull final List<Pattern> classNameIgnores) {
        return new StackTraceFocuser<>(classNameIgnores.stream().
                map(StackTraceFocuser::ignoreClassName).
                collect(toList()));
    }

    /**
     * Creates a new, default {@code StackTraceFocuser} ignoring frames from the JDK.
     *
     * @param <E> the exception type
     *
     * @return thew new {@code StackTraceFocuser}, never missing
     */
    @Nonnull
    public static <E extends Throwable> StackTraceFocuser<E> ignoreJavaClasses() {
        return ignoreClassNames(defaultClassNameIgnores);
    }

    /**
     * Constructs a new {@code StackTraceFocuser} for the given <var>ignores</var> predicates.  All
     * predicates are or'ed when checking if a frame should be ignored.
     *
     * @param ignores the predicates of frames to ignore, never missing
     */
    public StackTraceFocuser(@Nonnull final Iterable<Predicate<StackTraceElement>> ignores) {
        ignore = stream(ignores.spliterator(), false).
                reduce(Predicate::or).
                orElse(frame -> false).
                negate();
    }

    /**
     * Constructs a new {@code StackTraceFocuser} for the given <var>ignores</var> predicates.
     *
     * @param first the first predicate of frames to ignore, never missing
     * @param rest the optional remaining predicates of frames to ignore
     */
    @SafeVarargs
    public StackTraceFocuser(@Nonnull final Predicate<StackTraceElement> first,
            final Predicate<StackTraceElement>... rest) {
        this(Lists.asList(first, rest));
    }

    @Override
    public E apply(final E e) {
        stream(over(e, Objects::isNull, Throwable::getCause).spliterator(), true).
                forEach(x -> {
                    final List<StackTraceElement> found = asList(x.getStackTrace()).stream().
                            filter(ignore).
                            collect(toList());
                    x.setStackTrace(found.toArray(new StackTraceElement[found.size()]));
                });
        return e;
    }

    @Nonnull
    public static Predicate<StackTraceElement> ignoreClassName(@Nonnull final Pattern className) {
        return frame -> className.matcher(frame.getClassName()).find();
    }

    @Nonnull
    public static Predicate<StackTraceElement> ignoreMethodName(@Nonnull final Pattern methodName) {
        return frame -> methodName.matcher(frame.getMethodName()).find();
    }

    @Nonnull
    public static Predicate<StackTraceElement> ignoreFileName(@Nonnull final Pattern fileName) {
        return frame -> fileName.matcher(frame.getFileName()).find();
    }

    @Nonnull
    public static Predicate<StackTraceElement> ignoreLineNumber(@Nonnull final Pattern lineNumber) {
        return frame -> lineNumber.matcher(String.valueOf(frame.getLineNumber())).find();
    }
}
