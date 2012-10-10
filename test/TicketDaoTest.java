import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.exceptions.HectorException;
import models.Ticket;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import services.TicketDao;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static me.prettyprint.hector.api.factory.HFactory.createKeyspace;
import static me.prettyprint.hector.api.factory.HFactory.getOrCreateCluster;

/**
 * @author Guillaume ALAUX <guillaume at alaux dot net>
 *         Date: 10/8/12
 */
public class TicketDaoTest {

    public static final String CLUSTER_NAME = "Ticketex-TEST-cluster";
    public static final String KEYSPACE_NAME = "ksp_ticket_test";
    public static final String CLUSTER_URI = "127.0.0.1:9160";

    private static String FILE_LOCATION = "/home/miguel/documents/it/ticketex/sources/test_Ticket.script";
    public static final String CASSANDRA_CLI_PATH = "/home/miguel/documents/it/ticketex/tools/cassandra-1.1.2/bin/cassandra-cli";

    // Note that the name is only for Hector to identify it and it is not linked to the real Cassandra CLUSTER name.
    private Cluster cluster;
    private Keyspace keyspace;
    private TicketDao ticketDao;

    @Before
    public void setup() throws IOException {
        // Load DB content for test from file
        ProcessBuilder pb = new ProcessBuilder(CASSANDRA_CLI_PATH, "-f", FILE_LOCATION);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        Process p = pb.start();

        cluster = getOrCreateCluster(CLUSTER_NAME, CLUSTER_URI);
        keyspace = createKeyspace(KEYSPACE_NAME, cluster);
        ticketDao = new TicketDao(cluster, keyspace);

        // Just to let Cassandra "settle"
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() {
        try {
            cluster.dropKeyspace(KEYSPACE_NAME);
        } catch (HectorException e) {
            // Just to catch "Cannot drop non existing keyspace"
        }
    }

    @Test
    public void testCreate() {
        Ticket ticket = new Ticket();
        ticket.setLabel("LABEL");
        ticket.setDescription("DESCRIPTION");
        ticket.setPrice(new Long(34));
        Date end = new Date(123);
        ticket.setEnd(end);
        Date start = new Date(5678);
        ticket.setStart(start);

        ticketDao.create(ticket);

        Assert.assertNotNull(ticket);
        Assert.assertNotNull(ticket.getId());
        Assert.assertEquals(34l, ticket.getPrice().longValue());
    }

    @Test
    public void testFind() {
        Ticket ticket = ticketDao.find(UUID.fromString("0-0-0-0-1"));
        Assert.assertNotNull(ticket);
        Assert.assertEquals(UUID.fromString("0-0-0-0-1"), ticket.getId());
    }

    @Test
    public void testAll() {
        int count = 1000;
        List<Ticket> tickets = ticketDao.all(count);
        Assert.assertEquals(10, tickets.size());
    }

    @Test
    public void testDelete() {
        Assert.assertNotNull(ticketDao.find(UUID.fromString("0-0-0-0-2")));
        ticketDao.delete(UUID.fromString("0-0-0-0-2"));
        Assert.assertNull(ticketDao.find(UUID.fromString("0-0-0-0-2")));
    }

    @Test
    public void testUpdate() {
        Ticket ticket = ticketDao.find(UUID.fromString("0-0-0-0-2"));
        Assert.assertEquals(12l, ticket.getPrice().longValue());
        ticket.setPrice(42L);
        ticketDao.update(ticket);

        Ticket newTicket = ticketDao.find(UUID.fromString("0-0-0-0-2"));
        Assert.assertEquals(42l, newTicket.getPrice().longValue());
    }
}
