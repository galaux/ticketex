package services;

import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.ExceptionCallback;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.MutationBatch;
import com.netflix.astyanax.connectionpool.NodeDiscoveryType;
import com.netflix.astyanax.connectionpool.OperationResult;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.astyanax.connectionpool.impl.CountingConnectionPoolMonitor;
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.ColumnList;
import com.netflix.astyanax.model.Row;
import com.netflix.astyanax.model.Rows;
import com.netflix.astyanax.serializers.StringSerializer;
import com.netflix.astyanax.serializers.UUIDSerializer;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;
import com.netflix.astyanax.util.RangeBuilder;
import models.Ticket;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Guillaume ALAUX <guillaume at alaux dot net>
 *         Date: 10/12/12
 */
public class AstyanaxTicketDao implements TicketDao {

    public static final String CONNECTION_POOL_NAME = "ConnectionPool";

    private static final ColumnFamily<UUID, String> CF_TICKET =
            new ColumnFamily<UUID, String>(
                    CF_TICKET_NAME,            // Column Family Name
                    UUIDSerializer.get(),     // Key Serializer
                    StringSerializer.get());  // Column Serializer

    private Keyspace keyspace;

    public AstyanaxTicketDao(String clusterName, String clusterHost, int clusterPort, String keyspaceName) {
        AstyanaxContext<Keyspace> context = new AstyanaxContext.Builder()
                .forCluster(clusterName)
                .forKeyspace(keyspaceName)
                .withAstyanaxConfiguration(new AstyanaxConfigurationImpl()
                        .setDiscoveryType(NodeDiscoveryType.NONE)
                )
                .withConnectionPoolConfiguration(new ConnectionPoolConfigurationImpl(CONNECTION_POOL_NAME)
                        .setPort(clusterPort)
                        .setMaxConnsPerHost(1)
                        .setSeeds(clusterHost)
                )
                .withConnectionPoolMonitor(new CountingConnectionPoolMonitor())
                .buildKeyspace(ThriftFamilyFactory.getInstance());

        context.start();
        keyspace = context.getEntity();
    }

    public Ticket find(UUID id) {
        Ticket ticket = null;
        OperationResult<ColumnList<String>> result = null;
        try {
            result = keyspace.prepareQuery(CF_TICKET)
                    .getKey(id)
                    .execute();
        } catch (ConnectionException e) {
            throw new RuntimeException(e);
        }

        ColumnList<String> columns = result.getResult();
        if (!columns.isEmpty()) {
            // Lookup columns in response by name
            ticket = new Ticket();
            ticket.setId(id);
            ticket.setLabel(columns.getColumnByName(TICKET_LABEL).getStringValue());
            ticket.setDescription(columns.getColumnByName(TICKET_DESCRIPTION).getStringValue());
            ticket.setPrice(columns.getColumnByName(TICKET_PRICE).getLongValue());
        }

//        // Or, iterate through the columns
//        for (Column<String> c : result.getResult()) {
//            System.out.println(c.getName());
//        }

        return ticket;
    }

    @Override
    public void create(Ticket ticket) {
        MutationBatch m = keyspace.prepareMutationBatch();

        UUID id = UUID.randomUUID();
        m.withRow(CF_TICKET, id)
                .putColumn(TICKET_LABEL, ticket.getLabel(), null)
                .putColumn(TICKET_DESCRIPTION, ticket.getDescription(), null)
                .putColumn(TICKET_PRICE, ticket.getPrice(), null);
        try {
            OperationResult<Void> result = m.execute();
        } catch (ConnectionException e) {
            throw new RuntimeException(e);
        }

        ticket.setId(id);
    }

//    @Override
//    public List<Ticket> getPage(int pageSize, boolean reversed, UUID id) {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }

//    @Override
//    public ListTicketResult all(int pageSize, UUID firstRequiredId, UUID currPageFirstId) {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }

    @Override
    public List<Ticket> all() {
        List<Ticket> tickets = new ArrayList<Ticket>();
        try {
            OperationResult<Rows<UUID, String>> rows = keyspace.prepareQuery(CF_TICKET)
                    .getAllRows()
//                    .setRowLimit(pageSize)  // This is the page size
                    .withColumnRange(new RangeBuilder().setLimit(3).build())
                    .setExceptionCallback(new ExceptionCallback() {
                        @Override
                        public boolean onException(ConnectionException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .execute();

            for (Row<UUID, String> row : rows.getResult()) {
                Ticket ticket = new Ticket();
                ticket.setId(row.getKey());
                ticket.setLabel(row.getColumns().getColumnByName(TICKET_LABEL).getStringValue());
                ticket.setDescription(row.getColumns().getColumnByName(TICKET_DESCRIPTION).getStringValue());
                ticket.setPrice(row.getColumns().getColumnByName(TICKET_PRICE).getLongValue());
                tickets.add(ticket);
            }
        } catch (ConnectionException e) {
            throw new RuntimeException(e);
        }
        return tickets;
    }

    @Override
    public void delete(UUID id) {
        MutationBatch m = keyspace.prepareMutationBatch();

        m.withRow(CF_TICKET, id).delete();
        try {
            OperationResult<Void> result = m.execute();
        } catch (ConnectionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Ticket ticket) {
        MutationBatch m = keyspace.prepareMutationBatch();

        m.withRow(CF_TICKET, ticket.getId())
                .putColumn(TICKET_LABEL, ticket.getLabel(), null)
                .putColumn(TICKET_DESCRIPTION, ticket.getDescription(), null)
                .putColumn(TICKET_PRICE, ticket.getPrice(), null);
        try {
            OperationResult<Void> result = m.execute();
        } catch (ConnectionException e) {
            throw new RuntimeException(e);
        }
    }
}

