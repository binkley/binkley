package lab.dynafig;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * {@code Tracking} tracks string key-value pairs, tracking external updates
 * to the boxed values.  Optionally tracks them as a type converted from
 * string.  Example use: <pre>
 * Tracking dynafig = ...;
 * Optional&lt;AtomicReference&lt;String&gt;&gt; prop =
 *         dynafig.track("prop");
 * boolean propDefined = prop.isPresent();
 * AtomicReference&lt;String&gt; propRef = prop.get();
 * String propValue = propRef.get();
 * // External source updates key-value pair for "prop"
 * String newPropValue = propRef.get();</pre>
 * <p>
 * In an injection context: <pre>
 * class Wheel {
 *     private final AtomicInteger rapidity;
 *
 *     &#64;Inject
 *     public Wheel(final Tracking dynafig) {
 *         rapidity = dynafig.track("rapidity").
 *                 orElseThrow(() -&gt; new IllegalStateException(
 *                         "Missing 'rapidity' property));
 *     }
 *
 *     public void spin() {
 *         spinAtRate(rapidity.get());
 *     }
 * }</pre>
 *
 * @author <a href="mailto:boxley@thoughtworks.com">B. K. Oxley</a>
 * @see Updating Updating key-value pairs
 * @see DefaultDynafig Reference implementation
 */
public interface Tracking {
    /**
     * Tracks the given <var>key</var> value as a string.  Returns empty if
     * <var>key</var> is undefined.  If <var>key</var> is defined, may stil
     * return a {@code null} boxed value.
     *
     * @param key the key, never missing
     *
     * @return the optional atomic value string, never missing
     */
    @Nonnull
    default Optional<AtomicReference<String>> track(
            @Nonnull final String key) {
        return track(key, IGNORE);
    }

    /**
     * Tracks the given <var>key</var> value as a string.  Returns empty if
     * <var>key</var> is undefined.  If <var>key</var> is defined, may stil
     * return a {@code null} boxed value.  Notifies <var>onUpdate</var> when
     * the tracked value changes, including this call.
     *
     * @param key the key, never missing
     * @param onUpdate the notification callback, never missing
     *
     * @return the optional atomic value string, never missing
     */
    @Nonnull
    Optional<AtomicReference<String>> track(@Nonnull final String key,
            @Nonnull final BiConsumer<String, ? super String> onUpdate);

    /**
     * Tracks the given <var>key</var> value as a boolean.  Returns empty if
     * <var>key</var> is undefined.
     *
     * @param key the key, never missing
     *
     * @return the optional atomic value boolean, never missing
     */
    @Nonnull
    default Optional<AtomicBoolean> trackBool(@Nonnull final String key) {
        return trackBool(key, IGNORE);
    }

    /**
     * Tracks the given <var>key</var> value as a boolean.  Returns empty if
     * <var>key</var> is undefined.  Notifies <var>onUpdate</var> when the
     * tracked value changes, including this call.
     *
     * @param key the key, never missing
     * @param onUpdate the notification callback, never missing
     *
     * @return the optional atomic value boolean, never missing
     */
    @Nonnull
    Optional<AtomicBoolean> trackBool(@Nonnull final String key,
            @Nonnull final BiConsumer<String, ? super Boolean> onUpdate);

    /**
     * Tracks the given <var>key</var> value as an integer.  Returns empty if
     * <var>key</var> is undefined.
     *
     * @param key the key, never missing
     *
     * @return the optional atomic value integer, never missing
     */
    @Nonnull
    default Optional<AtomicInteger> trackInt(@Nonnull final String key) {
        return trackInt(key, IGNORE);
    }

    /**
     * Tracks the given <var>key</var> value as an integer.  Returns empty if
     * <var>key</var> is undefined.  Notifies <var>onUpdate</var> when the
     * tracked value changes, including this call.
     *
     * @param key the key, never missing
     * @param onUpdate the notification callback, never missing
     *
     * @return the optional atomic value integer, never missing
     */
    @Nonnull
    Optional<AtomicInteger> trackInt(@Nonnull final String key,
            @Nonnull final BiConsumer<String, ? super Integer> onUpdate);

    /**
     * Tracks the given <var>key</var> value as a long integer.  Returns empty
     * if <var>key</var> is undefined.
     *
     * @param key the key, never missing
     *
     * @return the optional atomic value long, never missing
     */
    @Nonnull
    default Optional<AtomicLong> trackLong(@Nonnull final String key) {
        return trackLong(key, IGNORE);
    }

    /**
     * Tracks the given <var>key</var> value as a long integer.  Returns empty
     * if <var>key</var> is undefined.  Notifies <var>onUpdate</var> when the
     * tracked value changes, including this call.
     *
     * @param key the key, never missing
     * @param onUpdate the notification callback, never missing
     *
     * @return the optional atomic value long, never missing
     */
    @Nonnull
    Optional<AtomicLong> trackLong(@Nonnull final String key,
            @Nonnull final BiConsumer<String, ? super Long> onUpdate);

    /**
     * Tracks the given <var>key</var> value as <var>type</var>.  Returns
     * empty if <var>key</var> is undefined.  If <var>key</var> is defined,
     * may still return a {@code null} boxed value.
     *
     * @param key the key, never missing
     * @param convert the value converter, never missing
     * @param <T> the value type
     *
     * @return the optional atomic value reference, never missing
     */
    @Nonnull
    default <T> Optional<AtomicReference<T>> trackAs(
            @Nonnull final String key,
            @Nonnull final Function<String, T> convert) {
        return trackAs(key, convert, IGNORE);
    }

    /**
     * Tracks the given <var>key</var> value as <var>type</var>.  Returns
     * empty if <var>key</var> is undefined.  If <var>key</var> is defined,
     * may still return a {@code null} boxed value.  Notifies
     * <var>onUpdate</var> when the tracked value changes, including this
     * call.
     *
     * @param key the key, never missing
     * @param convert the value converter, never missing
     * @param onUpdate the notification callback, never missing
     * @param <T> the value type
     *
     * @return the optional atomic value reference, never missing
     */
    @Nonnull
    <T> Optional<AtomicReference<T>> trackAs(@Nonnull final String key,
            @Nonnull final Function<String, T> convert,
            @Nonnull final BiConsumer<String, ? super T> onUpdate);

    /** Ignores value changes. */
    BiConsumer<String, Object> IGNORE = (k, v) -> {
    };
}
