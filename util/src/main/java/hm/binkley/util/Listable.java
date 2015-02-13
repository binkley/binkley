package hm.binkley.util;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * {@code Listable} supports types exposing themselves as a list view without
 * implementing the {@link List} interface.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
@FunctionalInterface
public interface Listable<T> {
    /**
     * Provides a list view.
     *
     * @return the list view, never missing
     */
    @Nonnull
    List<T> list();
}
