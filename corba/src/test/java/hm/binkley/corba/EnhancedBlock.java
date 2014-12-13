package hm.binkley.corba;

import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import javax.annotation.Nonnull;

import static hm.binkley.util.Mixin.Factory.newMixin;

/**
 * {@code EnhancedBlock} mixes {@link BlockOperations} and {@link BlockExtras}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public interface EnhancedBlock
        extends BlockOperations, BlockExtras {
    static EnhancedBlock from(@Nonnull final CORBAHelper helper)
            throws InvalidName, NotFound, CannotProceed {
        final BlockOperations corba = helper
                .resolve("Block", BlockHelper::narrow);
        // Proxy first so we don't interrogate CORBA for methods unless
        // necessary
        return newMixin(EnhancedBlock.class, new Default(corba), corba);
    }
}
