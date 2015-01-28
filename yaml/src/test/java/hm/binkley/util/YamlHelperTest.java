package hm.binkley.util;

import hm.binkley.util.YamlHelper.Builder;
import lombok.EqualsAndHashCode;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nonnull;
import java.util.Random;

import static java.util.stream.IntStream.rangeClosed;
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
    public void shouldLoadWithImplicit() {
        final Yaml yaml = YamlHelper.builder().
                then(Foo::registerWith).
                build();

        final Object o = yaml.load("3x12");
        assertThat(o, is(instanceOf(Foo.class)));
        final Foo foo = (Foo) o;
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
        assertThat(doc, is(equalTo("--- 3x12\n...\n")));
    }

    @Test
    public void shouldLoadWithExtraImplicit() {
        final Yaml yaml = YamlHelper.builder().
                then(FancyFoo::registerWith).
                build();

        final Object o = yaml.load("x20");
        assertThat(o, is(instanceOf(FancyFoo.class)));
        final FancyFoo foo = (FancyFoo) o;
        assertThat(foo.number(), is(equalTo(1)));
        assertThat(foo.sides(), is(equalTo(20)));
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

        final Object o = yaml.load("!* howard jones");
        assertThat(o, is(instanceOf(Bar.class)));
        final Bar bar = (Bar) o;
        assertThat(bar.value(), is(equalTo("howard jones")));
    }

    public static final class Foo {
        @Language("RegExp")
        private static final String match
                = "^([123456789]\\d*)x(4|6|8|10|12|20|100)$";
        private static final YamlHelper<Foo> helper = YamlHelper
                .from("123456789", match, Integer::valueOf, Integer::valueOf,
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
        private static final YamlHelper<FancyFoo> helper = YamlHelper
                .from("x123456789", match, FancyFoo::nullableIntegerValueOf,
                        Integer::valueOf, FancyFoo::new,
                        "'%s' is not the *fancy* foo you are looking for");
        private static final Random random = new Random();

        public static void registerWith(final YamlHelper.Builder builder) {
            builder.addImplicit(FancyFoo.class, helper);
        }

        @Nullable
        private final Integer number;
        private final int sides;

        @Nonnull
        public static FancyFoo valueOf(@Nonnull final String val) {
            return helper.valueOf().apply(val);
        }

        public FancyFoo(@Nullable final Integer number, final int sides) {
            this.number = number;
            this.sides = sides;
        }

        public int number() {
            return null == number ? 1 : number;
        }

        public int sides() {
            return sides;
        }

        public int roll() {
            return rangeClosed(1, number()).
                    map(n -> random.nextInt(sides()) + 1).
                    sum();
        }

        @Override
        public String toString() {
            return null == number ? "x" + sides : number + "x" + sides;
        }

        private static Integer nullableIntegerValueOf(final String n) {
            return null == n ? null : Integer.valueOf(n);
        }
    }

    public static final class Bar {
        public static void registerWith(final Builder builder) {
            // http://www.fileformat.info/info/unicode/char/1f3b2/index.htm
            // not permitted by SnakeYAML.  In fact, SnakeYAML does not like
            // anyone not using ASCII.
            builder.addExplicit(Bar.class, "*");
        }

        @Nonnull
        public static Bar valueOf(final String val) {
            return new Bar(val);
        }

        public Bar(@Nonnull final String value) {
            this.value = value;
        }

        private final String value;

        @Nonnull
        public String value() {
            return value;
        }
    }
}
