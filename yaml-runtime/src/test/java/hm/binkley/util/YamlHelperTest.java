package hm.binkley.util;

import hm.binkley.util.YamlHelper.Builder;
import lombok.EqualsAndHashCode;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Nullable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;

import static hm.binkley.util.YamlHelper.implicitFrom;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.yaml.snakeyaml.DumperOptions.FlowStyle.FLOW;
import static org.yaml.snakeyaml.DumperOptions.ScalarStyle.PLAIN;

/**
 * {@code YamlHelperTest} tests {@link YamlHelper}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley</a>
 */
public class YamlHelperTest {
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldLoadWithImplicit() {
        final Yaml yaml = YamlHelper.builder().
                then(Foo::registerWith).
                build();

        final Foo foo = (Foo) yaml.load("3x12");
        assertThat(foo.bar, is(equalTo(3)));
        assertThat(foo.none, is(equalTo(12)));
    }

    @Test
    public void shouldLoadAsWithImplicit() {
        // SnakeYAML wants new(x) and ignores Constructor for loadAs
        thrown.expectMessage(containsString(
                "No single argument constructor found for class hm.binkley.util.YamlHelperTest$Foo"));

        final Yaml yaml = YamlHelper.builder().
                then(Foo::registerWith).
                build();

        final Foo foo = yaml.loadAs("3x12", Foo.class);
        assertThat(foo.bar, is(equalTo(3)));
        assertThat(foo.none, is(equalTo(12)));
    }

    @Test
    public void shouldDumpWithImplicit() {
        final Yaml yaml = YamlHelper.builder().
                then(Foo::registerWith).
                build();

        final Foo foo = new Foo(3, 12);
        final String doc = yaml.dump(foo);
        assertThat(doc, is(equalTo("3x12\n")));
    }

    @Test
    public void shouldLoadWithExtraImplicit() {
        final Yaml yaml = YamlHelper.builder().
                then(FancyFoo::registerWith).
                build();

        final FancyFoo foo = (FancyFoo) yaml.load("x20");
        assertThat(foo.bar(), is(equalTo(1)));
        assertThat(foo.none(), is(equalTo(20)));
    }

    @Test
    public void shouldLoadithValueOf() {
        assertThat(FancyFoo.valueOf("3x6"), is(equalTo(new FancyFoo(3, 6))));
    }

    @Test
    public void shouldLoadWithExplicit() {
        final Yaml yaml = YamlHelper.builder().
                then(Bar::registerWith).
                build();

        // TODO: Why does loadAs behave differently than load+cast?
        final Bar bar = yaml.loadAs("!* howard jones", Bar.class);
        assertThat(bar.value(), is(equalTo("howard jones")));
    }

    @Test
    public void shouldDumpWithExplicit() {
        // SnakeYAML treats PLAIN style as single-quoted :(
        thrown.expect(AssertionError.class);
        thrown.expectMessage("Expected: is \"!* howard jones\\n\"\n"
                + "     but: was \"!* 'howard jones'\\n\"");

        final Yaml yaml = YamlHelper.builder().
                then(Bar::registerWith).
                build();

        assertThat(yaml.dump(new Bar("howard jones")),
                is(equalTo("!* howard jones\n")));
    }

    @Test
    public void shouldChangeStyles() {
        final Yaml yaml = YamlHelper.builder().
                then(Bar::registerWith).
                build(options -> {
                    options.setDefaultFlowStyle(FLOW);
                    options.setDefaultScalarStyle(PLAIN);
                    options.setWidth(Integer.MAX_VALUE);
                });

        assertThat(yaml.dump(new LinkedHashMap<String, Object>(4) {{
            put("a", "aaa");
            put("b", 3);
            put("c", asList("CCC", 12, true));
            put("d", new LinkedHashMap<String, Object>(2) {{
                put("p", "PPP");
                put("q", 4);
            }});
        }}), is(equalTo(
                "{a: aaa, b: 3, c: [CCC, 12, true], d: {p: PPP, q: 4}}\n")));
    }

    public static final class Foo {
        @Language("RegExp")
        private static final String match
                = "^([123456789]\\d*)x(4|6|8|10|12|20|100)$";
        private static final YamlHelper.Implicit<Foo> helper = implicitFrom(
                "123456789", match, Integer::valueOf, Integer::valueOf,
                Foo::new, "'%s' is not the foo you are looking for");

        public static void registerWith(final YamlHelper.Builder builder) {
            builder.addImplicit(Foo.class, helper);
        }

        public final int bar;
        public final int none;

        public Foo(final int bar, final int none) {
            this.bar = bar;
            this.none = none;
        }

        @Override
        public String toString() {
            return bar + "x" + none;
        }
    }

    @EqualsAndHashCode
    public static final class FancyFoo {
        @Language("RegExp")
        private static final String match
                = "^([123456789]\\d*)?x(4|6|8|10|12|20|100)$";
        private static final YamlHelper.Implicit<FancyFoo> helper
                = implicitFrom("x123456789", match,
                FancyFoo::nullableIntegerValueOf, Integer::valueOf,
                FancyFoo::new,
                "'%s' is not the *fancy* foo you are looking for");

        public static void registerWith(final YamlHelper.Builder builder) {
            builder.addImplicit(FancyFoo.class, helper);
        }

        @Nullable
        private final Integer bar;
        private final int none;

        @Nonnull
        public static FancyFoo valueOf(@Nonnull final String val) {
            return helper.valueOf().apply(val);
        }

        public FancyFoo(@Nullable final Integer bar, final int none) {
            this.bar = bar;
            this.none = none;
        }

        public int bar() {
            return null == bar ? 1 : bar;
        }

        public int none() {
            return none;
        }

        @Override
        public String toString() {
            return null == bar ? "x" + none : bar + "x" + none;
        }

        private static Integer nullableIntegerValueOf(final String n) {
            return null == n ? null : Integer.valueOf(n);
        }
    }

    public static final class Bar {
        private static final YamlHelper.Explicit<Bar> helper = YamlHelper
                .explicitFrom(Bar::new);

        public static void registerWith(final Builder builder) {
            // http://www.fileformat.info/info/unicode/char/1f3b2/index.htm
            // not permitted by SnakeYAML.  In fact, SnakeYAML does not like
            // anyone not using ASCII.
            builder.addExplicit(Bar.class, "*", helper);
        }

        public Bar(@Nonnull final String value) {
            this.value = value;
        }

        private final String value;

        @Nonnull
        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
