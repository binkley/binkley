package hm.binkley.annotation.processing.y;

import hm.binkley.util.Listable;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static hm.binkley.annotation.processing.Utils.typeFor;
import static hm.binkley.annotation.processing.Utils.valueFor;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

/**
 * {@code YMethod} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public final class YMethod
        extends YBlock
        implements Listable<YProperty> {
    public final String rtype;
    public final Object value;
    private final List<YProperty> properties;

    YMethod(final Yaml yaml,
            final Map.Entry<String, Map<String, Object>> raw) {
        super(yaml, raw);
        final Map<String, Object> values = raw.getValue();
        rtype = (String) values
                .computeIfAbsent("type", k -> typeFor(values.get("value")));
        value = values.computeIfAbsent("value", k -> valueFor(rtype));
        properties = values.entrySet().stream().
                map(YProperty::new).
                collect(toList());
    }

    @Nonnull
    @Override
    public List<YProperty> list() {
        return unmodifiableList(properties);
    }

    @Override
    public String toString() {
        return format("YMethod:%2$s{%1$s}=%3$s", name, rtype, value);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        final YMethod that = (YMethod) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
