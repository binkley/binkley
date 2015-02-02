package hm.binkley.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
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
    /**
     * Resource path to the template from which to generate sources.  Defaults
     * to {@code /generate-java.ftl}.
     */
    String template() default "/generate-java.ftl";

    /** Resource paths to YAML used by the template (wildcards supported). */
    String[] inputs();

    /** Package name for generated Java code.  Defaults to top-level. */
    String namespace() default "";

    @Documented
    @Inherited
    @Retention(RUNTIME)
    @Target({FIELD, METHOD, TYPE})
    @interface Definition {
        /**
         * Key/value pairs of the YAML definition for this method.  Ideally
         * this would be a sequence of pairs of string to Object, but
         * annotations do not support that.  Values in the pair are their
         * natural {@code toString()} representation.
         */
        String[] value();
    }

    @Documented
    @Inherited
    @Retention(RUNTIME)
    @Target(METHOD)
    @interface Documentation {
        /**
         * Documentation from YAML, same as the {@code "doc"} value in {@link
         * Definition} repeated separately so the annotation is inherited.
         * Extending YAML classes may declare overrides to change values, and
         * inherit the documentation.
         */
        String value();
    }
}
