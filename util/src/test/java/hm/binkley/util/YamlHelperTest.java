package hm.binkley.util;

import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@code YamlHelperTest} tests {@link YamlHelper}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley</a>
 */
public class YamlHelperTest {
    @Test
    public void shouldLoadWithAddImplicit() {
        final Yaml yaml = YamlHelper.builder().
                then(Foo::registerWith).
                build();
        final Object o = yaml.load("3x007");
        assertThat(o, is(instanceOf(Foo.class)));
        final Foo foo = (Foo) o;
        assertThat(foo.bar, is(equalTo(3)));
        assertThat(foo.none, is(equalTo(7)));
    }

    @Test
    public void shouldDumpWithAddImplicit() {
        final Yaml yaml = YamlHelper.builder().
                then(Foo::registerWith).
                build();
        final Foo dice = new Foo(3, 7);
        final String doc = yaml.dump(dice);
        assertThat(doc, is(equalTo("--- 3x7\n...\n")));
    }

    public static final class Foo {
        private static final String n = "123456789";
        private static final YamlHelper<Foo> helper = YamlHelper
                .from(n, "^([" + n + "]\\d*)x(\\d+)$", Foo::new,
                        "'%s' is not the foo you are looking for");
        public final int bar;
        public final int none;

        public static void registerWith(final YamlHelper.Builder builder) {
            builder.addImplicit(Foo.class, helper);
        }

        public Foo(final int bar, final int none) {
            this.bar = bar;
            this.none = none;
        }

        @Override
        public String toString() {
            return bar + "x" + none;
        }
    }
}
