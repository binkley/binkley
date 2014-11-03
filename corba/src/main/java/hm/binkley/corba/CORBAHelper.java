package hm.binkley.corba;

import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.omg.PortableServer.Servant;

import javax.annotation.Nonnull;
import java.util.Properties;

/**
 * {@code Narrow} simplifies ORB object narrowing.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public final class CORBAHelper
        implements Runnable {
    private static final Properties useJacorb = new Properties();

    static {
        useJacorb.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        useJacorb.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
    }

    private final ORB orb;
    private final POA root;
    private final NamingContextExt nameService;

    public interface Narrower<T> {
        /**
         * Narrows the given CORBA object <var>reference</var>.
         *
         * @param reference the object reference, never missing
         *
         * @return the narrowed object
         */
        T narrow(@Nonnull final org.omg.CORBA.Object reference);
    }

    public static ORB jacorb(final String... args) {
        return ORB.init(args, useJacorb);
    }

    /**
     * Constructs a new {@code CORBAHelper} for the given <var>orb</var>.
     *
     * @param orb the CORBA ORB, never missing
     */
    public CORBAHelper(@Nonnull final ORB orb) {
        try {
            this.orb = orb;
            root = initial(orb, "RootPOA", POAHelper::narrow);
            root.the_POAManager().activate();
            nameService = initial(orb, "NameService", NamingContextExtHelper::narrow);
        } catch (final AdapterInactive | InvalidName e) {
            throw new Error("Bad CORBA", e);
        }
    }

    /**
     * Gets the orb.
     *
     * @return the ORB, never missing
     */
    @Nonnull
    public ORB orb() {
        return orb;
    }

    /**
     * Runs the ORB, blocking until it shuts down.
     *
     * @see ORB#run()
     */
    @Override
    public void run() {
        orb.run();
    }

    /**
     * Narrows an initial reference from the ORB by <var>name</var>.
     *
     * @param <T> the object type
     * @param orb the ORB, never missing
     * @param name the object name, never missing
     * @param narrower the narrowing function, never missing
     *
     * @return the narrowed object
     *
     * @throws InvalidName if <var>nae</var> is not found by the ORB
     */
    public static <T> T initial(@Nonnull final ORB orb, @Nonnull final String name,
            @Nonnull final Narrower<T> narrower)
            throws InvalidName {
        return narrower.narrow(orb.resolve_initial_references(name));
    }

    /**
     * Narrows a servant reference from the POA with the given <var>implementation</var>.
     *
     * @param <T> the object type
     * @param <S> the servant type
     * @param implementation the object implementation, never missing
     * @param narrower the narrowing function, never missing
     *
     * @return the narrowed object
     *
     * @throws ServantNotActive if the servant is inactive
     * @throws WrongPolicy TODO what is WrongPolicy
     */
    public <T, S extends Servant> T servant(@Nonnull final S implementation,
            @Nonnull final Narrower<T> narrower)
            throws ServantNotActive, WrongPolicy {
        return narrower.narrow(root.servant_to_reference(implementation));
    }

    /**
     * Rebinds the given <var>implementation</var> object to <var>name</var>.
     *
     * @param <T> the object type
     * @param <S> the servant type
     * @param name the rebound name, never missing
     * @param implementation the object implementaiton, never missing
     * @param narrower the narrowing function, never missing
     *
     * @throws org.omg.CosNaming.NamingContextPackage.InvalidName if <var>name</var> is invalid
     * @throws NotFound TODO what is not NotFound
     * @throws CannotProceed TODO what is CannotProceed
     * @throws ServantNotActive if the servant is inactive
     * @throws WrongPolicy TODO what is WrongPolicy
     */
    public <T extends org.omg.CORBA.Object, S extends Servant> void rebind(
            @Nonnull final String name, @Nonnull final S implementation,
            @Nonnull final Narrower<T> narrower)
            throws org.omg.CosNaming.NamingContextPackage.InvalidName, NotFound, CannotProceed,
            ServantNotActive, WrongPolicy {
        nameService.rebind(nameService.to_name(name), servant(implementation, narrower));
    }

    /**
     * Resolves the given <var>name</var> to an object.
     *
     * @param <T> the object type
     * @param name the object name, never missing
     * @param narrower the narrowing function, never missing
     *
     * @return the resolved object
     *
     * @throws org.omg.CosNaming.NamingContextPackage.InvalidName if <var>name</var> is invalid
     * @throws CannotProceed TODO what is CannotProceed
     * @throws NotFound if <var>name</var> is not bound
     */
    public <T> T resolve(@Nonnull final String name, @Nonnull final Narrower<T> narrower)
            throws CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName, NotFound {
        return narrower.narrow(nameService.resolve_str(name));
    }
}
