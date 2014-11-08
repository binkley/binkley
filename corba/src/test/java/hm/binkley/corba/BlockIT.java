package hm.binkley.corba;

import hm.binkley.corba.EnhancedBlock.Factory;
import hm.binkley.junit.ProvidePort;
import hm.binkley.util.logging.osi.OSI;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.Collectors;

import static hm.binkley.corba.CORBAHelper.jacorb;
import static hm.binkley.util.Arrays.array;
import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static java.util.concurrent.ForkJoinPool.commonPool;
import static java.util.stream.StreamSupport.stream;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

/**
 * {@code BlockIT} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public final class BlockIT {
    static {
        OSI.enable();
    }

    @Rule
    public final ProvidePort port = new ProvidePort();
    @Rule
    public final ProvideSystemProperty sysprops = new ProvideSystemProperty().
            and("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB").
            and("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton").
            // TODO: Why is ORB port ignored by server/client?
                    and("OAPort", String.valueOf(port.port())).
            and("OAIAddr", "127.0.0.1");

    private ForkJoinTask<Void> nameServerThread;
    private ForkJoinTask<Object> orbThread;
    private CORBAHelper helper;

    @Before
    public void setUp() {
        currentThread().setName("CORBA Block Client");

        nameServerThread = commonPool().submit(() -> {
            currentThread().setName("CORBA Name Server");
            Class.forName("org.jacorb.naming.NameServer").getMethod("main", String[].class)
                    // Funny cast keeps array from being seen as varargs
                    // TODO: Why is ORB port ignored by server/client?
                    .invoke(null, (Object) array("-DOAPort=38693" /*"-DOAPort=" + port.port()*/,
                            "-DOAIAddr=127.0.0.1"));
            return null;
        });

        helper = new CORBAHelper(jacorb());
        orbThread = commonPool().submit(() -> {
            currentThread().setName("CORBA ORB");
            helper.run();
            return null;
        });
    }

    @After
    public void tearDown() {
        orbThread.cancel(true);
        nameServerThread.cancel(true);
    }

    @Test
    public void should()
            throws InvalidName, CannotProceed, NotFound, ServantNotActive, WrongPolicy {
        final List<String> elements = asList("Foo!", "Bar!");

        helper.rebind("Block", new BlockService(helper.orb(), elements), BlockHelper::narrow);

        try (final EnhancedBlock block = Factory.from(helper)) {
            // TODO: MethodHandle invocation in the proxy thinks iterator() is (Object[])Object
            // assertThat(block, contains(elements.toArray()));
            assertThat(stream(block.spliterator(), false).
                    collect(Collectors.<String>toList()), contains(elements.toArray()));
        }
    }
}
