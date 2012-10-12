import services.AstyanaxTicketDao;

/**
 * @author Guillaume ALAUX <guillaume at alaux dot net>
 *         Date: 10/12/12
 */
public class AstyanaxTicketDaoTest extends GenericTicketDaoTest {

    @Override
    public void init() {
        ticketDao = new AstyanaxTicketDao(CLUSTER_NAME, CLUSTER_HOST, CLUSTER_PORT, KEYSPACE_NAME);
    }
}
