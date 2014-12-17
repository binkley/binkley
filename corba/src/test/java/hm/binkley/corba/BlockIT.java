package hm.binkley.corba;

import hm.binkley.junit.ProvidePort;
import hm.binkley.util.logging.osi.OSI;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.StandardErrorStreamLog;
import org.junit.contrib.java.lang.system.StandardOutputStreamLog;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.List;

import static hm.binkley.corba.BlockServer.runNameServer;
import static hm.binkley.corba.CORBAHelper.jacorb;
import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.net.InetAddress.getLocalHost;
import static java.util.Arrays.asList;
import static java.util.concurrent.ForkJoinPool.commonPool;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@code BlockIT} is an integration test of the {@link BlockClient sample
 * client} and {@link BlockServer sample server}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Only print out/err on failure - next version of system-rules
 */
@Ignore("Unable to get ORB wired to a random port")
public final class BlockIT {
    static {
        OSI.enable();
    }

    @Rule
    public final ProvidePort port = new ProvidePort();
    @Rule
    public final ProvideSystemProperty sysprops = new ProvideSystemProperty().
            and("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB").
            and("org.omg.CORBA.ORBSingletonClass",
                    "org.jacorb.orb.ORBSingleton").
            and("ORBInitRef.NameService",
                    format("corbaloc::localhost:%d/NameService", port.port())).
            and("OAPort", String.valueOf(port.port()));

    @Rule
    public final StandardOutputStreamLog out = new StandardOutputStreamLog();
    @Rule
    public final StandardErrorStreamLog err = new StandardErrorStreamLog();

    private CORBAHelper helper;

    @Before
    public void setUp()
            throws IOException {
        commonPool().submit(() -> {
            currentThread().setName("CORBA Name Service");
            runNameServer(port.port());
            return null;
        });

        // Block until name server ready
        while (true)
            try {
                new Socket(getLocalHost(), port.port()).close();
                break;
            } catch (final ConnectException ignored) {
                ignored.printStackTrace();
            }

        currentThread().setName("CORBA Block Client");
        System.out.println("PORT - " + port);
        helper = new CORBAHelper(jacorb("-DOAPort=" + port.port()));
        commonPool().submit(() -> {
            currentThread().setName("CORBA ORB");
            helper.run();
            return null;
        });
    }

    @After
    public void tearDown()
            throws Throwable {
        commonPool().shutdownNow();
    }

    @Test(timeout = 10000)
    public void shouldRoundtrip()
            throws InvalidName, CannotProceed, NotFound, ServantNotActive,
            WrongPolicy {
        final List<String> elements = asList("Foo!", "Bar!");

        helper.rebind("Block", new BlockService(helper.orb(), elements),
                BlockHelper::narrow);

        try (final EnhancedBlock block = EnhancedBlock.from(helper)) {
            assertThat(stream(block.spliterator(), false).
                    collect(toList()), is(equalTo(elements)));
        }
    }
}
