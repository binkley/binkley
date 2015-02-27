package hm.binkley.annotation.processing;

import org.springframework.core.io.DescriptiveResource;

/**
 * {@code UnLoaded} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public final class UnLoaded
        extends Loaded<Void> {
    public static UnLoaded unLoaded(final YamlGenerateMesseger out,
            final String format, final Object... args) {
        final String where = out.annoFormat(format, args);
        return new UnLoaded(where);
    }

    private UnLoaded(final String where) {
        super(where, new DescriptiveResource(where), null);
    }

    @Override
    public String where() {
        return where;
    }

    @Override
    public String toString() {
        return where();
    }
}
