package hm.binkley.corba;

/**
 * {@code HelloServer} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */

import hm.binkley.util.logging.osi.OSI;
import hm.binkley.corba.CORBAHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import static java.lang.System.setProperty;
import static java.util.concurrent.ForkJoinPool.commonPool;
import static hm.binkley.corba.CORBAHelper.jacorb;

public final class BlockServer {
    public static void main(final String... args)
            throws ServantNotActive, WrongPolicy, InvalidName, NotFound, CannotProceed {
        OSI.enable();

        setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");

        commonPool().submit(() -> {
            Class.forName("org.jacorb.naming.NameServer").getMethod("main", String[].class)
                    // Funny cast keeps array from being seen as varargs
                    .invoke(null, (Object) new String[]{"-DOAPort=38693"});
            return null;
        });

        final CORBAHelper helper = new CORBAHelper(jacorb(args));

        helper.rebind("Block", new BlockService(helper.orb(), "Foo!", "Bar!"), BlockHelper::narrow);

        helper.run();
    }
}
