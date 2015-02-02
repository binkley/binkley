package hm.binkley.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import static java.lang.String.format;
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
        /** Sequence of YAML used to define the annotated element. */
        String[] value();
    }

    @Documented
    @Inherited
    @Retention(RUNTIME)
    @Target({METHOD, TYPE})
    @interface Documentation {
        /**
         * Documentation from YAML, same as the {@code "doc"} value in {@link
         * Definition} repeated separately so the annotation is inherited.
         * Extending YAML classes may declare overrides to change values, and
         * inherit the documentation.
         */
        String value();
    }

    /**
     * Wraps helpers for {@link Definition} and {@link Documentation}; Java
     * does not support static methods on annotations as it does on
     * interfaces.
     */
    interface Helper {
        /** Extract annotated "doc" value from the element. */
        static String documentationFor(final Class<?> generatedType) {
            final Documentation anno = generatedType
                    .getAnnotation(Documentation.class);
            if (null == anno)
                throw new IllegalArgumentException(
                        format("No @%s annotation on %s",
                                Documentation.class.getName(),
                                generatedType.getName()));
            return anno.value();
        }

        /** Extract annotated "doc" value from the element. */
        static String documentationFor(final Class<?> generatedType,
                final String name)
                throws NoSuchMethodException {
            final Method method = generatedType.getMethod(name);
            final Documentation anno = method
                    .getAnnotation(Documentation.class);
            if (null == anno)
                throw new IllegalArgumentException(
                        format("No @%s annotation on %s::%s",
                                Documentation.class.getName(),
                                generatedType.getName(), name));
            return anno.value();
        }

        /** Extract annotated YAML definition for the element. */
        static String[] definitionFor(final Class<?> generatedType) {
            final Definition anno = generatedType
                    .getAnnotation(Definition.class);
            if (null == anno)
                throw new IllegalArgumentException(
                        format("No @%s annotation on %s",
                                Definition.class.getName(),
                                generatedType.getName()));
            return anno.value();
        }

        /** Extract annotated YAML definition for the element. */
        static String[] definitionFor(final Class<?> generatedType,
                final String name)
                throws NoSuchMethodException {
            final Method method = generatedType.getMethod(name);
            final Definition anno = method.getAnnotation(Definition.class);
            if (null == anno)
                throw new IllegalArgumentException(
                        format("No @%s annotation on %s::%s",
                                Definition.class.getName(),
                                generatedType.getName(), name));
            return anno.value();
        }
    }
}
