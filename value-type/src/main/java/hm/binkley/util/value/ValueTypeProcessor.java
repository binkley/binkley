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

package hm.binkley.util.value;

import org.kohsuke.MetaInfServices;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singleton;
import static java.util.regex.Pattern.compile;
import static javax.lang.model.SourceVersion.RELEASE_8;
import static javax.lang.model.element.ElementKind.INTERFACE;
import static javax.lang.model.element.ElementKind.PACKAGE;
import static javax.lang.model.util.ElementFilter.fieldsIn;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;
import static javax.tools.Diagnostic.Kind.WARNING;

/**
 * {@code ValueTypeProcessor} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
@MetaInfServices(Processor.class)
@SupportedAnnotationTypes("hm.binkley.util.value.ValueType")
@SupportedSourceVersion(RELEASE_8)
public final class ValueTypeProcessor
        extends AbstractProcessor {
    private static final Pattern packageVar = compile("\\$\\{package\\}");
    private static final Pattern classVar = compile("\\$\\{class\\}");
    private static final Pattern typeVar = compile("\\$\\{type\\}");

    private final String template;

    public ValueTypeProcessor() {
        try (final Scanner scanner = new Scanner(
                getClass().getResourceAsStream("value-type.java"), UTF_8.name())) {
            template = scanner.useDelimiter("\\A").next();
        }
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations,
            final RoundEnvironment roundEnv) {
        if (roundEnv.processingOver())
            return false;

        for (final String aName : getSupportedAnnotationTypes()) {
            ELEMEMT:
            for (final Element element : roundEnv.getElementsAnnotatedWith(
                    processingEnv.getElementUtils().getTypeElement(aName))) {
                final List<? extends AnnotationMirror> aMirrors = element.getAnnotationMirrors();
                for (final AnnotationMirror aMirror : aMirrors) {
                    if (!ValueType.class.getName().equals(aMirror.getAnnotationType().toString()))
                        continue;

                    final Messenger messenger = new Messenger(element, aMirror);

                    if (INTERFACE != element.getKind()) {
                        messenger.error("@ValueType only supported on interfaces");
                        continue;
                    }

                    if (PACKAGE != element.getEnclosingElement().getKind()) {
                        messenger.error("@ValueType only supported on top-level interfaces");
                        continue;
                    }

                    for (final VariableElement field : fieldsIn(singleton(element)))
                        if ("$cache".equals(field.getSimpleName().toString())) {
                            messenger.error("@ValueType duplicates '$cache' field");
                            continue ELEMEMT;
                        }

                    // Rely on knowledge there is only the value field
                    final AnnotationValue type = aMirror.getElementValues().values().stream().
                            findFirst().get();

                    final String packaj = processingEnv.getElementUtils().getPackageOf(element)
                            .getQualifiedName().toString();
                    final String clazz = element.getSimpleName().toString();
                    final String source = format("%s.%sValue", packaj, clazz);
                    try (final OutputStream out = processingEnv.getFiler()
                            .createSourceFile(source, element).openOutputStream()) {
                        String generated = packageVar.matcher(template).replaceAll(packaj);
                        generated = classVar.matcher(generated).replaceAll(clazz);
                        generated = typeVar.matcher(generated)
                                .replaceAll(type.getValue().toString());
                        out.write(generated.getBytes(UTF_8));
                    } catch (final IOException e) {
                        messenger.error("Cannot generate source: %s", e);
                        continue ELEMEMT;
                    }

                    messenger.note("Generated %s.java", source);
                }
            }
        }

        return true;
    }

    private final class Messenger {
        private final Element element;
        private final AnnotationMirror mirror;

        private Messenger(final Element element, final AnnotationMirror mirror) {
            this.element = element;
            this.mirror = mirror;
        }

        void error(final String format, final Object... args) {
            processingEnv.getMessager().printMessage(ERROR, format(format, args), element, mirror);
        }

        void warning(final String format, final Object... args) {
            processingEnv.getMessager().printMessage(WARNING, format(format, args), element, mirror);
        }

        void note(final String format, final Object... args) {
            processingEnv.getMessager().printMessage(NOTE, format(format, args), element, mirror);
        }
    }
}
