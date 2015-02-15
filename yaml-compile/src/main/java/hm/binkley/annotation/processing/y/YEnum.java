package hm.binkley.annotation.processing.y;

import org.yaml.snakeyaml.Yaml;

import java.util.Map;

import static java.lang.String.format;

/**
 * {@code YEnum} models enums.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public final class YEnum
        extends YBlock {
    YEnum(final Yaml yaml, final Map.Entry<String, Map<String, Object>> raw) {
        super(yaml, raw);
    }

    @Override
    public String toString() {
        return format("YEnum{%s}", name);
    }
}
