package hm.binkley.corba;

import hm.binkley.corba.CORBAHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import javax.annotation.Nonnull;

import static hm.binkley.util.Mixin.Factory.newMixin;

/**
 * {@code EnhancedBlock} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public interface EnhancedBlock
        extends BlockOperations, BlockExtras {
    final class Factory {
        public static EnhancedBlock from(@Nonnull final CORBAHelper helper)
                throws InvalidName, NotFound, CannotProceed {
            final BlockOperations corba = helper.resolve("Block", BlockHelper::narrow);
            // Proxy first so we don't interrogate CORBA for methods unless necessary
            return newMixin(EnhancedBlock.class, new BlockExtras.Default(corba), corba);
        }
    }
}
