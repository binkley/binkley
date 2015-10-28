package hm.binkley.util.property;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static hm.binkley.util.property.Property.getter;
import static hm.binkley.util.property.Property.in;
import static hm.binkley.util.property.Property.on;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@code PropertyTest} tests {@link Property}.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 */
@RunWith(Parameterized.class)
public class PropertyTest {
    @Parameter(0)
    public Property<String> p;

    @Parameters(name = "{index}: {0}")
    public static Collection<Property<String>[]> parameters() {
        final Box box = new Box();
        final String[] array = {null};
        final List<String> list = new ArrayList<>(1);
        list.add(null);
        final Map<Integer, String> map = new HashMap<>(1);

        return asList(args(Property.valueOf(null)),
                args(getter(box::getS).setter(box::setS)),
                args(in(array).at(0)), args(in(list).at(0)),
                args(in(map).at(3)),
                args(on(box).getter(Box::getS).setter(Box::setS)));
    }

    @Test
    public void shouldSetAndGet() {
        p.set("a");
        assertThat(p.get(), is(equalTo("a")));
    }

    @Test
    public void shouldForEach() {
        p.set("b");
        final List<String> values = new ArrayList<>(1);
        p.forEach(values::add);
        assertThat(values, is(equalTo(singletonList("b"))));
    }

    @Test
    public void shouldMap() {
        p.set("c");
        assertThat(p.map(value -> value.charAt(0)), is(equalTo('c')));
    }

    @Test
    public void shouldFlatMap() {
        p.set("d");
        assertThat(
                p.flatMap(value -> Property.valueOf(value.charAt(0))).get(),
                is(equalTo('d')));
    }

    @Test
    public void shouldAccept() {
        p.accept("e");
        assertThat(p.get(), is(equalTo("e")));
    }

    @Test
    public void shouldView() {
        p.accept("3");
        final Property<Integer> view = p.
                view(Integer::valueOf, String::valueOf);
        assertThat(view.get(), is(equalTo(3)));
        view.set(4);
        assertThat(p.get(), is(equalTo("4")));
    }

    @Test
    public void shouldCollectWithValue() {
        p.set("g");
        assertThat(p.collect().get(), is(equalTo("g")));
    }

    @Test
    public void shouldCollectWithNull() {
        p.set(null);
        assertThat(p.collect().isPresent(), is(false));
    }

    @Test
    public void shouldCollect2() {
        p.set("i");
        assertThat(p.collect(toList()), is(equalTo(singletonList("i"))));
    }

    @Test
    public void shouldCollect3() {
        p.set("j");
        assertThat(p.collect(ArrayList::new, List::add),
                is(equalTo(singletonList("j"))));
    }

    @Test
    public void shouldIterate() {
        p.set("k");
        final List<String> values = new ArrayList<>(1);
        for (final String value : p)
            values.add(value);
        assertThat(values, is(equalTo(singletonList("k"))));
    }

    @SuppressWarnings("unchecked")
    private static Property<String>[] args(final Property<String> property) {
        return (Property<String>[]) new Property[]{property};
    }

    public static final class Box {
        @lombok.Getter
        @lombok.Setter
        private String s;
    }
}
