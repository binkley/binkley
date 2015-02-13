package hm.binkley.annotation.processing.y;

import org.yaml.snakeyaml.Yaml;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;

/**
 * {@code YDocumented} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
abstract class YDocumented {
    private static final Pattern DQUOTE = compile("\"");
    public final String name;
    public final String doc;
    public final String escapedDoc;
    public final List<String> definition;

    protected YDocumented(final String name, final String doc,
            final Yaml yaml, final Map<String, ?> block) {
        this.name = name;
        this.doc = doc;
        escapedDoc = null == doc ? null
                : DQUOTE.matcher(escapeJava(doc)).replaceAll("\\\"");
        definition = toAnnotationValue(yaml, block);
    }

    protected void putInto(final Map<String, Object> model) {
        model.put("name", name);
        model.put("doc", doc);
        model.put("escapedDoc", escapedDoc);
        model.put("definition", definition);
    }

    private static List<String> toAnnotationValue(final Yaml yaml,
            final Map<String, ?> props) {
        return null == props ? emptyList() : props.entrySet().stream().
                map(e -> singletonMap(e.getKey(), e.getValue())).
                map(e -> toQuotedYaml(yaml, e)).
                collect(toList());
    }

    private static String toQuotedYaml(final Yaml yaml, final Object value) {
        final String dumped = yaml.dump(value);
        return format("\"%s\"",
                escapeJava(dumped.substring(0, dumped.length() - 1)));
    }
}
