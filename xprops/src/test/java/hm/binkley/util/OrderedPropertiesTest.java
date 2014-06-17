/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util;

import org.junit.Test;

import java.util.List;
import java.util.Properties;

import static hm.binkley.util.OrderedProperties.Ordering.UPDATED;
import static java.util.Collections.reverse;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class OrderedPropertiesTest {
    /** @todo This a stochastic test - how to make it deterministic? */
    @Test
    public void shouldPreserveDefinedOrder() {
        final Properties properties = new OrderedProperties();
        final int size = 1024;
        final List<String> keys = range(0, size).mapToObj(String::valueOf).collect(toList());
        keys.forEach(key -> properties.put(key, key));
        assertThat(keys, is(equalTo(properties.stringPropertyNames().stream().collect(toList()))));
    }

    /** @todo This a stochastic test - how to make it deterministic? */
    @Test
    public void shouldPreserveUpdatedOrder() {
        final Properties properties = new OrderedProperties(UPDATED);
        final int size = 1024;
        final List<String> keys = range(0, size).mapToObj(String::valueOf).collect(toList());
        keys.forEach(key -> properties.put(key, key));
        reverse(keys);
        keys.forEach(key -> properties.put(key, key));
        assertThat(keys, is(equalTo(properties.stringPropertyNames().stream().collect(toList()))));
    }
}
