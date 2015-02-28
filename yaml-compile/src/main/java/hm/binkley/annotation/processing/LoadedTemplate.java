package hm.binkley.annotation.processing;

import freemarker.template.Template;
import org.springframework.core.io.Resource;

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
}
