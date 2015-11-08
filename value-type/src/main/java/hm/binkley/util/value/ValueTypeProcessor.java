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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.tools.Diagnostic.Kind;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Instant.now;
import static java.util.regex.Pattern.compile;
import static javax.lang.model.SourceVersion.RELEASE_8;
import static javax.lang.model.element.ElementKind.INTERFACE;
import static javax.lang.model.element.ElementKind.PACKAGE;
import static javax.lang.model.element.Modifier.STATIC;
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
    private static final Pattern baseVar = compile("\\$\\{base\\}");
    private static final Pattern typeVar = compile("\\$\\{type\\}");
    private static final Pattern modifyVar = compile("\\$\\{modify\\}");
    private static final Pattern timestamp = compile("\\$\\{timestamp\\}");

    private final String template;

    public ValueTypeProcessor()
            throws IOException {
        final URL source = getClass().getResource("value-type.java");
        try (final Scanner scanner = new Scanner(source.openStream(),
                UTF_8.name()).useDelimiter("\\A")) {
            if (!scanner.hasNext())
                throw new IllegalStateException(
                        format("Illegal template source: %s", source));
            template = scanner.next();
        }
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations,
            final RoundEnvironment roundEnv) {
        ELEMENT:
        for (final Element element : roundEnv.getElementsAnnotatedWith(
                processingEnv.getElementUtils().getTypeElement(ValueType.class.getName()))) {
            final List<? extends AnnotationMirror> aMirrors = element.getAnnotationMirrors();
            for (final AnnotationMirror mirror : aMirrors) {
                if (!ValueType.class.getName().equals(mirror.getAnnotationType().toString()))
                    continue;

                final Messenger messenger = new Messenger(element, mirror);

                if (INTERFACE != element.getKind()) {
                    messenger.error("@ValueType only supported on interfaces");
                    continue;
                }

                if (PACKAGE != element.getEnclosingElement().getKind()) {
                    messenger.error("@ValueType only supported on top-level interfaces");
                    continue;
                }

                ((TypeElement) element).getEnclosedElements().stream().
                        map(ExecutableElement.class::cast).
                        filter(ValueTypeProcessor::unsupported).
                        forEach(m -> messenger
                                .error("@ValueType supports only static and default methods: %s",
                                        m));

                // Rely on knowing there is only the value field
                final AnnotationValue value = mirror.getElementValues().values().stream().
                        findFirst().get();

                final String packaj = processingEnv.getElementUtils().getPackageOf(element)
                        .getQualifiedName().toString();
                final String clazz = element.getSimpleName().toString();
                final String source = format("%s.%sValue", packaj, clazz);
                final DeclaredType valueType = (DeclaredType) value.getValue();
                final String type = valueType.toString();
                final String modify = String.class.getName().equals(type) ? ".intern()" : "";

                final boolean comparable = comparable(valueType);
                final String base = comparable ? "ComparableValue" : "Value";

                try (final OutputStream out = processingEnv.getFiler()
                        .createSourceFile(source, element).openOutputStream()) {
                    String generated = packageVar.matcher(template).replaceAll(packaj);
                    generated = classVar.matcher(generated).replaceAll(clazz);
                    generated = baseVar.matcher(generated).replaceAll(base);
                    generated = typeVar.matcher(generated).replaceAll(type);
                    generated = modifyVar.matcher(generated).replaceAll(modify);
                    generated = timestamp.matcher(generated).replaceAll(now().toString());
                    out.write(generated.getBytes(UTF_8));
                } catch (final IOException e) {
                    messenger.error("Cannot generate source: %s", e);
                    continue ELEMENT;
                }

                messenger.note("Generated %s.java", source);
            }
        }

        return true;
    }

    private static boolean unsupported(final ExecutableElement e) {
        return !(e.isDefault() || e.getModifiers().contains(STATIC));
    }

    private static boolean comparable(final DeclaredType type) {
        final String comparable = format("java.lang.Comparable<%s>", type);
        return ((TypeElement) type.asElement()).getInterfaces().stream().
                map(Object::toString).
                filter(comparable::equals).
                findFirst().
                isPresent();
    }

    private final class Messenger {
        private final Element element;
        private final AnnotationMirror mirror;

        private Messenger(final Element element, final AnnotationMirror mirror) {
            this.element = element;
            this.mirror = mirror;
        }

        void error(final String format, final Object... args) {
            message(ERROR, format, args);
        }

        void warning(final String format, final Object... args) {
            message(WARNING, format, args);
        }

        void note(final String format, final Object... args) {
            message(NOTE, format, args);
        }

        private void message(final Kind kind, final String format, final Object... args) {
            processingEnv.getMessager().printMessage(kind, format(format, args), element, mirror);
        }
    }
}
