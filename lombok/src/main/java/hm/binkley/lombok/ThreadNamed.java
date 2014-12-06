package hm.binkley.lombok;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * {@code ThreadNamed} sets the thread name during method execution, restoring it when the method
 * completes (normally or exceptionally).
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
@Documented
@Retention(SOURCE)
@Target({CONSTRUCTOR, METHOD})
public @interface ThreadNamed {
    /** The name for the thread while the annotated method executes. */
    String value();
}
