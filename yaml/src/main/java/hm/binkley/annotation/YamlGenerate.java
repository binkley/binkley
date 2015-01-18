package hm.binkley.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * {@code GenerateFromTemplate} marks a dummy type.  The point is to generate
 * from the template {@link #template()} using {@link #inputs()} to define
 * classes in YAML.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
@Documented
@Retention(SOURCE)
@Target(TYPE)
public @interface YamlGenerate {
    /** Resource path to the template from which to generate sources. */
    String template();

    /** Resource paths to YAML used by the template (wildcards supported). */
    String[] inputs();

    /** Package name for generated Java code.  Defaults to top-level. */
    String namespace() default "";

    @Documented
    @Inherited
    @Retention(RUNTIME)
    @interface Definition {
        /**
         * Key/value pairs of the YAML definition for this method.  Ideally
         * this would be a sequence of pairs of string to Object, but
         * annotations do not support that.  Values in the pair are their
         * natural {@code toString()} representation.
         */
        String[] value();
    }
}
