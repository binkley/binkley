package hm.binkley.configuration;

import javax.annotation.Nonnull;

import static java.lang.String.format;

/**
 * {@code AbstractPropertyConfiguration} needs documentation.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public abstract class AbstractPropertyConfiguration<T extends AbstractPropertyConfiguration<T, K, V, E>, K, V, E extends Exception>
        implements Configuration<T, K, V, E> {
    protected final PropertiesLoader<E> loader;
    protected final PropertyFetcher<K, V, E> fetcher;

    protected AbstractPropertyConfiguration(@Nonnull final PropertiesLoader<E> loader,
            @Nonnull final PropertyFetcher<K, V, E> fetcher) {
        this.loader = loader;
        this.fetcher = fetcher;
    }

    @Override
    public V lookup(@Nonnull final K key)
            throws E {
        return fetcher.fetch(loader.load(), key);
    }

    @Nonnull
    public String misskingKeyMessage(@Nonnull final K key) {
        return format("Cannot find %s in: %s", fetcher.describe(key), loader.describe());
    }
}
