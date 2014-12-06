package hm.binkley.lombok;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Formatter;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * {@code ThreadNamed} sets the thread name during method or constructor execution, restoring it
 * when the method or constructor completes (normally or exceptionally).
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
@Documented
@Retention(SOURCE)
@Target({CONSTRUCTOR, METHOD})
public @interface ThreadNamed {
    /**
     * The name for the thread while the annotated method or constructor executes.  {@code value} is
     * treated as a formatted string for {@link String#format(String, Object...)} with any method or
     * constructor parameters passed in as formatting args.  Use <em>positional notation</em> to
     * skip parameters, or they may be ignroed entirely in which case {@code value} is treated as a
     * plain string.
     * <p>
     * Example with formatting: <pre>
     * &64;ThreadNamed("Foo #%1$s")
     * public void doFoo(final String name, final int slot, final Object data) {
     *     // Do something interesting with method parameters
     * }</pre> when called with {@code "apple", 2, 3.14159} will set the current thread name to
     * "Foo #2" while {@code doFoo} executes.
     *
     * @see Formatter
     */
    String value();
}
