package hm.binkley.annotation.processing;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;

/**
 * {@code LoadedYaml} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
@SuppressWarnings("unchecked")
public final class LoadedYaml
        extends Loaded<Map<String, Map<String, Map<String, Object>>>> {
    public final String path;

    LoadedYaml(final String pathPattern, final Resource whence,
            final Map<String, Map<String, Map<String, Object>>> yaml,
            final List<String> roots)
            throws IOException {
        super(pathPattern, whence, (Map) escapeJavaStringsInYaml(yaml));
        path = path(pathPattern, whence, roots);
    }

    private static Object escapeJavaStringsInYaml(final Object yaml) {
        if (yaml instanceof Map) {
            final Set<Entry<String, Object>> entries
                    = ((Map<String, Object>) yaml).entrySet();
            for (final Entry<String, Object> e : entries)
                e.setValue(escapeJavaStringsInYaml(e.getValue()));
        } else if (yaml instanceof List) {
            final ListIterator<Object> it = ((List<Object>) yaml).
                    listIterator();
            while (it.hasNext())
                it.set(escapeJavaStringsInYaml(it.next()));
        } else if (yaml instanceof String)
            return escapeJava((String) yaml);

        return yaml;
    }

    @Override
    public String where() {
        return path;
    }
}
