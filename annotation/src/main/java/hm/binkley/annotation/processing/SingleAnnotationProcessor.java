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

package hm.binkley.annotation.processing;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@code SingleAnnotationProcessor} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public abstract class SingleAnnotationProcessor<A extends Annotation, M extends SingleAnnotationMessager<A, M>>
        extends AbstractProcessor {
    private final Class<A> annoType;
    // TODO: Rework to avoid writeable state
    protected M out;

    protected SingleAnnotationProcessor(final Class<A> annoType) {
        this.annoType = annoType;
    }

    protected abstract M newMesseger(final Class<A> annoType,
            final Messager messager, final Element element);

    protected boolean preValidate(final Element element) {
        return true;
    }

    protected abstract void process(final Element element, final A annno);

    protected String withAnnotationValue() {
        return null;
    }

    protected boolean postValidate() {
        return true;
    }

    @Override
    public final boolean process(final Set<? extends TypeElement> annotations,
            final RoundEnvironment roundEnv) {
        for (final Element element : roundEnv
                .getElementsAnnotatedWith(annoType)) {
            out = newMesseger(annoType, processingEnv.getMessager(), element);

            if (!preValidate(element))
                continue;

            // Use both annotation and mirror:
            // - Annotation is easier for accessing members
            // - Mirror is needed for messenger

            final AnnotationMirror aMirror = annotationMirror(element);
            if (null == aMirror)
                continue;

            // Optionally narrow down logging to specific anno value
            final String annotationValue = withAnnotationValue();
            if (null != annotationValue)
                out = out.withAnnotation(aMirror,
                        annotationValue(aMirror, annotationValue));

            try {
                process(element, element.getAnnotation(annoType));
            } catch (final Exception e) {
                out.error(e, "Cannot process %@ on '%s'", element);
            }
        }

        return postValidate();
    }

    private AnnotationMirror annotationMirror(final Element element) {
        // TODO: How to do this without resorting to toString()?
        final List<AnnotationMirror> found = element.
                getAnnotationMirrors().stream().
                filter(m -> m.getAnnotationType().
                        toString().
                        equals(annoType.getCanonicalName())).
                collect(Collectors.<AnnotationMirror>toList());

        switch (found.size()) {
        case 1:
            return found.get(0);
        case 0:
            out.error("%@ missing from element");
            return null;
        default:
            out.error("%@ only supports 1 occurrence");
            return null;
        }
    }

    protected static AnnotationValue annotationValue(
            final AnnotationMirror aMirror, final String param) {
        return aMirror.getElementValues().entrySet().stream().
                filter(e -> e.getKey().getSimpleName().contentEquals(param)).
                map(Map.Entry::getValue).
                findAny().
                orElse(null);
    }
}
