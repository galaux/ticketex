import services.HectorTicketDao;

/**
 * @author Guillaume ALAUX <guillaume at alaux dot net>
 *         Date: 10/8/12
 */
public class HectorTicketDaoTest extends GenericTicketDaoTest {

    @Override
    public void init() {
        ticketDao = new HectorTicketDao(CLUSTER_NAME, CLUSTER_HOST, CLUSTER_PORT, KEYSPACE_NAME);
    }
}
