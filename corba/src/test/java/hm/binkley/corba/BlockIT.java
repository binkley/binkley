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

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.Collectors;

import static hm.binkley.corba.CORBAHelper.jacorb;
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
            and("OAPort", String.valueOf(port.port()));

    private ForkJoinTask<Object> orbThread;
    private CORBAHelper helper;
    private Process server;

    @Before
    public void setUp()
            throws IOException {
        currentThread().setName("CORBA Block Client");

        final ProcessBuilder server = new ProcessBuilder("java", "-cp",
                System.getProperty("java.class.path"),
                "-Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB",
                "-Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton",
                "-DOAPort=" + port.port(), BlockServer.class.getName()).
                inheritIO();
        this.server = server.start();

        // Block until name server ready
        while (true)
            try {
                new Socket("localhost", port.port()).close();
                break;
            } catch (final ConnectException ignored) {
                ignored.printStackTrace();
            }

        helper = new CORBAHelper(jacorb("-DOAPort=" + port.port()));
        orbThread = commonPool().submit(() -> {
            currentThread().setName("CORBA ORB");
            if (false)
                helper.run();
            return null;
        });
    }

    @After
    public void tearDown()
            throws Throwable {
        final Throwable orbError = orbThread.getException();
        orbThread.cancel(true);
        server.destroy();
        if (null != orbError)
            throw orbError;
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
