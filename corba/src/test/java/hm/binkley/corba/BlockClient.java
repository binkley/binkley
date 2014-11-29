package hm.binkley.corba;

import hm.binkley.util.logging.osi.OSI;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import static java.lang.System.out;
import static hm.binkley.corba.CORBAHelper.jacorb;

/**
 * {@code HelloClient} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public final class BlockClient {
    static {
        OSI.enable();
    }

    public static void main(final String... args)
            throws CannotProceed, NotFound, InvalidName {
        final CORBAHelper helper = new CORBAHelper(jacorb(args));
        try (final EnhancedBlock block = EnhancedBlock.from(helper)) {
            block.forEach(out::println);
            block.stream().
                    map(String::length).
                    forEach(out::println);
        }
    }
}
