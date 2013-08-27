package hm.binkley.configuration;

import javax.annotation.Nonnull;

/**
 * {@code AbstractPropertyConfiguration} needs documentation.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public abstract class AbstractPropertyConfiguration<T extends AbstractPropertyConfiguration<T, E>, E extends Exception>
        implements Configuration<T, E> {
    private final PropertiesLoader<E> loader;
    private final PropertyFetcher<E> fetcher;

    protected AbstractPropertyConfiguration(@Nonnull final PropertiesLoader<E> loader,
            @Nonnull final PropertyFetcher<E> fetcher) {
        this.loader = loader;
        this.fetcher = fetcher;
    }

    @Override
    public String lookup(@Nonnull final Object key)
            throws E {
        return fetcher.fetch(loader.load(), key);
    }
}
