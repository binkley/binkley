package hm.binkley.annotation.processing;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * {@code Listable} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
@FunctionalInterface
public interface Listable<T> {
    @Nonnull
    List<T> list();
}
