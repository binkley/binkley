package hm.binkley.annotation;

import org.junit.Test;

import static hm.binkley.annotation.YamlGenerate.Helper.definitionFor;
import static hm.binkley.annotation.YamlGenerate.Helper.documentationFor;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@code YamlGenerateTest} tests {@link YamlGenerate}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public final class YamlGenerateTest {
    @Test
    public void shouldFindDefinitionOnClass() {
        assertThat(definitionFor(Annotated.class),
                is(arrayContaining("a=b", "c=2")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotFindDefinitionOnClass() {
        definitionFor(NotAnnotated.class);
    }

    @Test
    public void shouldFindDefinitionOnMethod()
            throws NoSuchMethodException {
        assertThat(definitionFor(Annotated.class, "annotated"),
                is(arrayContaining("p=q", "r=42")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotFindDefinitionOnMethod()
            throws NoSuchMethodException {
        definitionFor(NotAnnotated.class, "notAnnotated");
    }

    @Test
    public void shouldFindDocumentationOnClass() {
        assertThat(documentationFor(Annotated.class),
                is(equalTo("Shawn the Sheep")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotFindDocumentationOnClass() {
        documentationFor(NotAnnotated.class);
    }

    @Test
    public void shouldFindDocumentationOnMethod()
            throws NoSuchMethodException {
        assertThat(documentationFor(Annotated.class, "annotated"),
                is(equalTo("Billy Goat")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotFindDocumentationOnMethod()
            throws NoSuchMethodException {
        documentationFor(NotAnnotated.class, "notAnnotated");
    }

    @YamlGenerate.Definition({"a=b", "c=2"})
    @YamlGenerate.Documentation("Shawn the Sheep")
    private static final class Annotated {
        @YamlGenerate.Definition({"p=q", "r=42"})
        @YamlGenerate.Documentation("Billy Goat")
        public void annotated() {}
    }

    private static final class NotAnnotated {
        public void notAnnotated() {}
    }
}
