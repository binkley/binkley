package hm.binkley.annotation.processing;

/**
 * {@code UnLoaded} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public final class UnLoaded
        extends Loaded<Void> {
    UnLoaded(final YamlGenerateMesseger out, final String format,
            final Object... args) {
        super(out.annoFormat(format, args), null, null);
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
