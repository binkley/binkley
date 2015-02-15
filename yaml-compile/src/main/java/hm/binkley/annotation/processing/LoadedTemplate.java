package hm.binkley.annotation.processing;

import freemarker.template.Template;
import org.springframework.core.io.Resource;

import static java.lang.String.format;

/**
 * {@code LoadedTemplate} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public final class LoadedTemplate
        extends Loaded<Template> {
    LoadedTemplate(final String path, final Resource whence,
            final Template template) {
        super(path, whence, template);
    }

    @Override
    public String where() {
        return where;
    }

    @Override
    public String toString() {
        final String whence = this.whence.getDescription();
        final String where = where();
        return whence.equals(where) ? whence
                : format("%s(%s)", whence, where);
    }
}
