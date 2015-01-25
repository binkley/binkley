package hm.binkley.util;

import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.util.function.BiFunction;

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

        final Foo foo = new Foo(3, 7);
        final String doc = yaml.dump(foo);
        assertThat(doc, is(equalTo("--- 3x7\n...\n")));
    }

    @Test
    public void shouldLoadWithExtraAddImplicit() {
        final Yaml yaml = YamlHelper.builder().
                then(FancyFoo::registerWith).
                build();

        final Object o = yaml.load("x007");
        assertThat(o, is(instanceOf(FancyFoo.class)));
        final FancyFoo foo = (FancyFoo) o;
        assertThat(foo.bar(), is(equalTo(1)));
        assertThat(foo.none(), is(equalTo(7)));
    }

    public static final class Foo {
        private static final String n = "123456789";
        private static final YamlHelper<Foo> helper = YamlHelper
                .from(n, "^([" + n + "]\\d*)x(\\d+)$", Integer::valueOf,
                        Integer::valueOf, Foo::new,
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

    public static final class FancyFoo {
        private static final String n = "123456789";
        private static final YamlHelper<FancyFoo> helper = YamlHelper
                .from('x' + n, "^([" + n + "]\\d*)?x(\\d+)$",
                        FancyFoo::nullableValueOf, Integer::valueOf,
                        (BiFunction<Integer, Integer, FancyFoo>) FancyFoo::new,
                        "'%s' is not the *fancy* foo you are looking for");
        private final Integer bar;
        private final int none;

        public static void registerWith(final YamlHelper.Builder builder) {
            builder.addImplicit(FancyFoo.class, helper);
        }

        public FancyFoo(final Integer bar, final int none) {
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

        private static Integer nullableValueOf(final String n) {
            return null == n ? null : Integer.valueOf(n);
        }
    }
}
