package hm.binkley.util.property;

import lombok.AllArgsConstructor;
import lombok.ToString;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static hm.binkley.util.property.Property.getter;
import static hm.binkley.util.property.Property.in;
import static hm.binkley.util.property.Property.on;
import static hm.binkley.util.property.Property.simple;
import static java.lang.System.out;
import static java.util.stream.Collectors.toList;

public final class PropertyMain {
    public static void main(final String... args) {
        final AtomicReference<String> box = new AtomicReference<>("Em");
        final Property<String> p = getter(box::get).
                setter(box::set);
        dump(p);
        p.set("Flo");
        dump(p);
        final Property<String> p2 = on(box).
                getter(AtomicReference::get).
                setter(AtomicReference::set);
        dump(p2);
        p2.set("Val");
        dump(p2);

        final Property<String> q = in("Bob").at(0);
        dump(q);
        q.set("Sid");
        dump(q);

        final List<String> list = new ArrayList<>(1);
        list.add("Mel");
        final Property<String> r = in(list).at(0);
        dump(r);
        r.set("Vic");
        dump(r);

        final Object key = new Object() {
            @Override
            public String toString() {
                return "the-key";
            }
        };
        final Map<Object, String> map = new HashMap<>();
        map.put(key, "Ed");
        final Property<String> s = in(map).at(key);
        dump(s);
        s.set("Dre");
        dump(s);

        final Thing thing = new Thing("Jo");
        final Property<String> t = on(thing).
                getter(on -> on.s).
                setter((on, value) -> on.s = value);
        dump(t);
        t.set("Bo");
        dump(t);

        t.accept("3");
        out.println(t.collect(toList()));

        final Property<Integer> i = t.view(Integer::valueOf, String::valueOf);
        dump(i);
        i.set(4);
        dump(t);
        dump(i);

        final Property<Integer> j = t.
                flatMap(value -> simple(Integer.valueOf(value)));
        dump(j);
        j.set(5);
        dump(t);
        dump(j);
    }

    private static <T> void dump(final Property<T> property) {
        out.printf("%s <- %s%n", property.get(), property);
    }

    @AllArgsConstructor
    @ToString
    private static final class Thing {
        @Nonnull
        private String s;
    }
}
