package hm.binkley.corba;

import org.omg.CORBA.ORB;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * {@code BockService} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public final class BlockService
        extends BlockPOA {
    private final ORB orb;
    private final List<String> elements;

    public BlockService(final ORB orb, final List<String> elements) {
        this.orb = orb;
        this.elements = elements;
    }

    public BlockService(final ORB orb, final String... elements) {
        this(orb, asList(elements));
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
