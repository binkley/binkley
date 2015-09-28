package lab.dynafig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

/**
 * {@code Updating} updates key-value pairs.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">B. K. Oxley</a>
 * @see Tracking Tracking key-value pairs
 * @see DefaultDynafig Reference implementation
 */
@FunctionalInterface
public interface Updating
        extends BiConsumer<String, String> {
    /**
     * Updates a key-value pair with a new value or adds the pair if
     * <var>key</var> is undefined.
     * <p>
     * <strong>NB</strong> &mdash; Does not return any value to avoid the <a
     * href="http://en.wikipedia.org/wiki/Read-modify-write">read-modify-write</a>
     * race condition.
     *
     * @param key the key, never missing
     * @param value the value, possibly {@code null}
     */
    void update(@Nonnull final String key, @Nullable final String value);

    /**
     * Updates a key-value pair as a map entry for convenience.
     *
     * @param entry the entry, never missing
     *
     * @see #update(String, String)
     */
    default void update(@Nonnull final Entry<String, String> entry) {
        update(entry.getKey(), entry.getValue());
    }

    /**
     * Updates a set of key-value pairs for convenience.  Each key is
     * invidually updated in entry-set order.
     *
     * @param values the key-value set, never missing
     *
     * @see #update(String, String)
     */
    default void updateAll(@Nonnull final Map<String, String> values) {
        values.entrySet().stream().
                forEach(this::update);
    }

    /** Alias for {@link #update(String, String)}. */
    @Override
    default void accept(@Nonnull final String key,
            @Nullable final String value) {
        update(key, value);
    }
}
