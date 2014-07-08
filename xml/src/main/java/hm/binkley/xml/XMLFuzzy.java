package hm.binkley.xml;

import hm.binkley.util.Converter;
import org.w3c.dom.Node;

import javax.annotation.Nonnull;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;
import static java.lang.reflect.Proxy.newProxyInstance;
import static java.util.Arrays.asList;

/**
 * {@code RedoXMLBeam} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 * @todo Replace run-time conversion with annotation processor to fix at compile time
 * @see <a href="http://xml.org/">XMLBeam</a>
 * @see <a href="http://en.wikipedia.org/wiki/Little_Fuzzy"><cite>Little Fuzzy</cite></a>
 */
public final class XMLFuzzy
        implements InvocationHandler {
    private static final XPath xpath = XPathFactory.newInstance().newXPath();
    private static final Map<Method, XPathExpression> expressions = new ConcurrentHashMap<>();

    private final Node node;
    private final Converter converter;

    public static final class Factory {
        private final Converter converter;

        public Factory(final Converter converter) {
            this.converter = converter;
        }

        public <T> T of(@Nonnull final Class<T> itf, @Nonnull final Node node) {
            return XMLFuzzy.of(itf, node, converter);
        }
    }

    public static <T> T of(@Nonnull final Class<T> itf, @Nonnull final Node node,
            @Nonnull final Converter converter) {
        return itf.cast(newProxyInstance(itf.getClassLoader(), new Class[]{itf},
                new XMLFuzzy(node, converter)));
    }

    private XMLFuzzy(final Node node, final Converter converter) {
        this.node = node;
        this.converter = converter;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args)
            throws Throwable {
        return converter.convert(method.getReturnType(), expressions.
                computeIfAbsent(method, XMLFuzzy::compile).
                evaluate(node));
    }

    private static XPathExpression compile(@Nonnull final Method method) {
        final String expression = asList(method.getAnnotations()).stream().
                filter(From.class::isInstance).
                map(From.class::cast).
                findFirst().
                orElseThrow(() -> new MissingAnnotation(method)).
                value();
        try {
            return xpath.compile(expression);
        } catch (final XPathExpressionException e) {
            throw new BadXPath(method, expression, e);
        }
    }

    public static final class MissingAnnotation
            extends RuntimeException {
        private MissingAnnotation(final Method method) {
            super(format("Missing @X(xpath) annotation: %s", method));
        }
    }

    public static final class BadXPath
            extends RuntimeException {
        private BadXPath(final Method method, final String expression,
                final XPathExpressionException e) {
            super(format("Bad @X(xpath) annotation on '%s': %s: %s", method, expression,
                    e.getMessage()));
            setStackTrace(e.getStackTrace());
        }
    }
}
