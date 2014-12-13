package hm.binkley.corba;

import org.omg.CORBA.ORB;

import javax.annotation.Nonnull;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * {@code BockService} implement the sample CORBA application for a "block".
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public final class BlockService
        extends BlockPOA {
    private final ORB orb;
    private final List<String> elements;

    /**
     * Constructs a new {@code BlockService} for the given <var>orb</var> and
     * <var>elements</var>.
     *
     * @param orb the CORBA ORB, never missing
     * @param elements the elements of this "block", never missing
     */
    public BlockService(@Nonnull final ORB orb,
            @Nonnull final List<String> elements) {
        this(orb, elements.toArray(new String[elements.size()]));
    }

    /**
     * Constructs a new {@code BlockService} for the given <var>orb</var> and
     * <var>elements</var>.
     *
     * @param orb the CORBA ORB, never missing
     * @param elements the elements of this "block"
     */
    public BlockService(@Nonnull final ORB orb, final String... elements) {
        this.orb = orb;
        this.elements = asList(elements);
    }

    @Override
    public int nElements() {
        return elements.size();
    }

    @Override
    public String elementAt(final int index) {
        return elements.get(index);
    }

    @Override
    public void shutdown() {
        orb.shutdown(false);
    }
}
