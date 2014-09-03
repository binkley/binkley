package hm.binkley.xml.reflect;

import hm.binkley.util.Converter;
import hm.binkley.xml.reflect.XMLFuzzy.BadXPath;
import hm.binkley.xml.reflect.XMLFuzzy.MissingAnnotation;
import org.intellij.lang.annotations.Language;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;

import static hm.binkley.xml.reflect.XMLFuzzyTest.Top.XML;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@code XMLFuzzyTest} tests {@link XMLFuzzy}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public final class XMLFuzzyTest {
    private Top top;

    @Before
    public void setUp()
            throws ParserConfigurationException, IOException, SAXException {
        final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new InputSource(new StringReader(XML)));
        top = new XMLFuzzy.Factory(new Converter()).of(Top.class, document);
    }

    @Test
    public void shouldHandleString() {
        assertThat(top.a(), is(equalTo("apple")));
    }

    @Test
    public void shouldHandlePrimitiveInt() {
        assertThat(top.b(), is(equalTo(3)));
    }

    @Test
    public void shouldHandleRURI() {
        assertThat(top.c(), is(equalTo(URI.create("http://some/where"))));
    }

    @Test(expected = MissingAnnotation.class)
    public void shouldThrowOnMissingAnnotation() {
        top.d();
    }

    @Test(expected = BadXPath.class)
    public void shouldThrowOnBadXPath() {
        top.e();
    }

    public interface Top {
        @Language("XML")
        String XML = "<top><a>apple</a><b>3</b><c>http://some/where</c></top>";

        @From("//top/a")
        String a();

        @From("//top/b")
        int b();

        @From("//top/c")
        URI c();

        void d();

        @From("dis' ain't xpath")
        void e();
    }
}
