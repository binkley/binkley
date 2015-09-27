package hm.binkley.annotation.processing;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

/**
 * {@code Utils} is shared code.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Datetime and other YAML types
 */
public final class Utils {
    /** Friendlier casts using return type deduction. */
    @SuppressWarnings("unchecked")
    static <T> T cast(final Object o) {
        return (T) o;
    }

    /**
     * Gets YAML type name for Java object.
     *
     * @todo Is this in SnakeYAML?
     */
    public static String typeFor(final Object value) {
        if (value instanceof String)
            return "str";
        else if (value instanceof Boolean)
            return "bool";
        else if (value instanceof Integer)
            return "int";
        else if (value instanceof Double)
            return "float";
        else if (value instanceof List)
            return "seq";
        else if (value instanceof Map)
            return "pairs";
        else
            return value.getClass().getName();
    }

    public static Object valueFor(final String rtype) {
        switch(rtype) {
        case "str": return null;
        case "bool": return false;
        case "int": return 0;
        case "float": return 0d;
        case "seq": return emptyList();
        case "pairs": return emptyMap();
        default: return null;
        }
    }
}
