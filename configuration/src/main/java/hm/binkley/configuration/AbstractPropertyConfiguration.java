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
    private final PropertiesFetcher<E> fetcher;
    private final PropertyGetter<E> getter;

    protected AbstractPropertyConfiguration(@Nonnull final PropertiesFetcher<E> fetcher,
            @Nonnull final PropertyGetter<E> getter) {
        this.fetcher = fetcher;
        this.getter = getter;
    }

    @Override
    public String lookup(@Nonnull final Object key)
            throws E {
        return getter.get(fetcher.fetch(), key);
    }
}
