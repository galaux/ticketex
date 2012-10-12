import models.Ticket;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import services.TicketDao;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

/**
 * @author Guillaume ALAUX <guillaume at alaux dot net>
 *         Date: 10/12/12
 */
public abstract class GenericTicketDaoTest {

    public static final String CASSANDRA_CLI_PATH = "/home/miguel/documents/it/ticketex/tools/cassandra-1.1.2/bin/cassandra-cli";
    private static String SCRIPT_LOAD = "/home/miguel/documents/it/ticketex/sources/test/Ticket_insert.script";
    private static String SCRIPT_DROP = "/home/miguel/documents/it/ticketex/sources/test/Ticket_drop.script";

    public static final String CLUSTER_NAME = "Ticketex-TEST-cluster";
    public static final String CLUSTER_HOST = "127.0.0.1";
    public static final int CLUSTER_PORT = 9160;
    public static final String KEYSPACE_NAME = "ticketex_test";

    public static final int TOTAL_TICKET_COUNT = 15;

    TicketDao ticketDao;

    public abstract void init();

    @Before
    public void load() throws IOException {

        init();

        // Load DB content for test from file
        ProcessBuilder pb = new ProcessBuilder(CASSANDRA_CLI_PATH, "-f", SCRIPT_LOAD);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        Process p = pb.start();

        // Just to let Cassandra "settle"
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @After
    public void drop() throws IOException {
        ProcessBuilder pb = new ProcessBuilder(CASSANDRA_CLI_PATH, "-f", SCRIPT_DROP);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        Process p = pb.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
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

//    @Test
//    public void testAllInOneTime() {
//        ListTicketResult listTicketResult = ticketDao.all(TOTAL_TICKET_COUNT + 10, ticketDao.UUID_NULL, ticketDao.UUID_NULL);
//        Assert.assertEquals(TOTAL_TICKET_COUNT, listTicketResult.tickets.size());
//        Assert.assertEquals(ticketDao.UUID_NULL, listTicketResult.prevPageFirstId);
//        Assert.assertEquals(ticketDao.UUID_NULL, listTicketResult.nextPageFirstId);
//    }
//
//    @Test
//    public void testAllInOneTimeExactCount() {
//        ListTicketResult listTicketResult = ticketDao.all(TOTAL_TICKET_COUNT, ticketDao.UUID_NULL, ticketDao.UUID_NULL);
//        Assert.assertEquals(TOTAL_TICKET_COUNT, listTicketResult.tickets.size());
//        List<Ticket> tickets = listTicketResult.tickets;
//        Assert.assertEquals(ticketDao.UUID_NULL, listTicketResult.prevPageFirstId);
//        Assert.assertEquals(ticketDao.UUID_NULL, listTicketResult.nextPageFirstId);
//    }
//
//    @Test
//    public void testAllPagination() {
//        // First get the complete list of tickets (will be used for later "asserts")
//        List<Ticket> completeTicketList = ticketDao.all(TOTAL_TICKET_COUNT, ticketDao.UUID_NULL, ticketDao.UUID_NULL).tickets;
//
//        // Let's fix a page size
//        int pageSize = 3;
//        // And a page index to request
//        int requestedPageIndex = 2;
//
//        // Then:
//        int prevPageFirstIndex = (requestedPageIndex - 1) * pageSize;
//        int requestedPageFirstIndex = requestedPageIndex * pageSize;
//        int nextPageFirstIndex = (requestedPageIndex + 1) * pageSize;
//
//        Ticket prevPageFirst = completeTicketList.get(prevPageFirstIndex);
//        Ticket requestedPageFirst = completeTicketList.get(requestedPageFirstIndex);
//        Ticket nextPageFirstId = completeTicketList.get(nextPageFirstIndex);
//
//        // Issue tested call
//        ListTicketResult listTicketResult = ticketDao.all(pageSize, requestedPageFirst.getId(), prevPageFirst.getId());
//
//        Assert.assertEquals(pageSize, listTicketResult.tickets.size());
//
//        // Check we retrieved the right list
//        int i = requestedPageFirstIndex;
//        for (Ticket ticket : listTicketResult.tickets) {
//            Assert.assertEquals(completeTicketList.get(i++).getId(), ticket.getId());
//        }
//
//        Assert.assertEquals(prevPageFirst.getId(), listTicketResult.prevPageFirstId);
//        Assert.assertEquals(nextPageFirstId.getId(), listTicketResult.nextPageFirstId);
//    }
//
//    /**
//     * Check that iteration through paginated calls
//     * - ends up with all Tickets
//     * - shows no doubles
//     */
//    @Test
//    public void testAllThroughPaginationIteration() {
//
//        Map<String, Boolean> map = new HashMap<String, Boolean>();
//
//        int pageSize = 3;
//
//        UUID prevPageFirstId = ticketDao.UUID_NULL;
//        UUID lastTicketId = ticketDao.UUID_NULL;
//
//        ListTicketResult listTicketResult;
//        int i = 0;
//
//        do {
//            listTicketResult = ticketDao.all(pageSize, lastTicketId, prevPageFirstId);
//
//            System.out.println(listTicketResult.tickets);
//
//            // Gather all retrieved UUIDs in a hashmap and check we have never had them previously
//            for (Ticket ticket : listTicketResult.tickets) {
//                String idAsStr = ticket.getId().toString();
//                if (map.get(idAsStr) != null && map.get(idAsStr)) {
//                    // If "all" returned a row we have already had then it's a failure !
//                    Assert.fail();
//                } else {
//                    map.put(idAsStr, true);
//                }
//            }
//
//            // Set ids for next round
//            lastTicketId = listTicketResult.nextPageFirstId;
//            prevPageFirstId = listTicketResult.tickets.get(0).getId();
//
//            // "2 * TOTAL_TICKET_COUNT" is just an arbitrary stopper
//        } while (listTicketResult.nextPageFirstId != ticketDao.UUID_NULL && (i++ < 2 * TOTAL_TICKET_COUNT));
//
//        if (i == 30) {
//            throw new RuntimeException("Out of loop because reached max loop count.");
//        }
//
//        Assert.assertEquals(TOTAL_TICKET_COUNT, map.size());
//    }

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

//    @Test
//    public void testGetPage() {
//        List<Ticket> tickets;
//        int pageSize = 3;
//        UUID lastUUID = TicketDao.UUID_NULL;
//        do {
//            tickets = ticketDao.getPage(pageSize, true, lastUUID);
//            lastUUID = tickets.get(tickets.size() - 1).getId();
//        } while (tickets.size() == pageSize);
//    }
}
