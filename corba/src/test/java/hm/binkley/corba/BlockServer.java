package hm.binkley.corba;

import hm.binkley.util.logging.osi.OSI;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import java.lang.reflect.InvocationTargetException;

import static hm.binkley.corba.CORBAHelper.jacorb;
import static java.lang.System.setProperty;
import static java.util.concurrent.ForkJoinPool.commonPool;

/**
 * {@code BlockServer} is a sample CORBA server process using {@link
 * CORBAHelper}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Support ORB port other than hard-configured
 */
public final class BlockServer {
    public static void main(final String... args)
            throws ServantNotActive, WrongPolicy, InvalidName, NotFound,
            CannotProceed {
        OSI.enable();

        setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        setProperty("org.omg.CORBA.ORBSingletonClass",
                "org.jacorb.orb.ORBSingleton");

        commonPool().submit(() -> {
            runNameServer(38693);
            return null;
        });

        final CORBAHelper helper = new CORBAHelper(jacorb(args));

        helper.rebind("Block", new BlockService(helper.orb(), "Foo!", "Bar!"),
                BlockHelper::narrow);

        helper.run();
    }

    /** Package scope for testing. */
    static void runNameServer(final int port)
            throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, ClassNotFoundException {
        // Funny cast keeps array from being seen as varargs
        Class.forName("org.jacorb.naming.NameServer").
                getMethod("main", String[].class).
                invoke(null, (Object) new String[]{"-DOAPort=" + port});
    }
}
