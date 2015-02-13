package hm.binkley.annotation.processing.y;

import org.yaml.snakeyaml.Yaml;

import java.util.Map;

/**
 * {@code YBlock} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
abstract class YBlock
        extends YDocumented {
    protected YBlock(final Yaml yaml,
            final Map.Entry<String, Map<String, Object>> raw) {
        super(raw.getKey(), doc(raw), yaml, raw.getValue());
    }

    private static String doc(
            final Map.Entry<String, Map<String, Object>> raw) {
        final Map<String, Object> properties = raw.getValue();
        return null == properties ? null : (String) properties.get("doc");
    }

    @Override
    public abstract String toString();
}
