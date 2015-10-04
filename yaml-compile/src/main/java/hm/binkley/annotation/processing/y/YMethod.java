package hm.binkley.annotation.processing.y;

import hm.binkley.util.Listable;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static hm.binkley.annotation.processing.Utils.typeFor;
import static hm.binkley.annotation.processing.Utils.valueFor;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * {@code YMethod} models methods.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public final class YMethod
        extends YBlock
        implements Listable<YProperty> {
    /** The YAML return type. */
    @Nonnull
    public final String rtype;
    /** The fixed value. */
    @Nullable
    public final Object value;
    private final List<YProperty> properties;

    YMethod(final Yaml yaml,
            final Map.Entry<String, Map<String, Object>> raw) {
        super(yaml, raw);
        final Map<String, Object> values = raw.getValue();
        rtype = (String) values.
                computeIfAbsent("type", k -> typeFor(values.get("value")));
        value = values.
                computeIfAbsent("value", k -> valueFor(rtype));
        properties = values.entrySet().stream().
                map(YProperty::new).
                collect(toList());
    }

    /**
     * Gets the unmodifiable list of properties for the method.
     *
     * @return the properties, never missing or empty
     */
    @Nonnull
    @Override
    public List<YProperty> list() {
        return unmodifiableList(properties);
    }

    @Nonnull
    public Map<String, Object> asMap() {
        return properties.stream().
                collect(toMap(YProperty::name, YProperty::value));
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
