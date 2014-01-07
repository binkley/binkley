/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.inject;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

/**
 * {@code MainTest} tests {@link Main}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public class MainTest {
    private static Map<String, String> none() {
        return emptyMap();
    }

    private static Map<String, String> only(final String flag, final Object value) {
        return singletonMap(flag, String.valueOf(value));
    }
}
