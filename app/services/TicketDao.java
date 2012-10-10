package services;


import me.prettyprint.cassandra.serializers.ByteBufferSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.serializers.UUIDSerializer;
import me.prettyprint.cassandra.service.template.ColumnFamilyResult;
import me.prettyprint.cassandra.service.template.ColumnFamilyTemplate;
import me.prettyprint.cassandra.service.template.ColumnFamilyUpdater;
import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.OrderedRows;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.exceptions.HectorException;
import me.prettyprint.hector.api.mutation.MutationResult;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.RangeSlicesQuery;
import models.Ticket;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static me.prettyprint.hector.api.factory.HFactory.*;

/**
 * @author Guillaume ALAUX <guillaume at alaux dot net>
 *         Date: 10/3/12
 */
public class TicketDao {

//    // Note that the name is only for Hector to identify it and it is not linked to the real Cassandra cluster name.
//    private static final Cluster cluster = getOrCreateCluster(CLUSTER_NAME, CLUSTER_URI);
//    public static final Keyspace keyspace = createKeyspace(KEYSPACE_NAME, cluster);

    private Cluster cluster;
    private Keyspace keyspace;

    public static final String COLUMN_FAMILY = "Ticket";
    public static final String TICKET_LABEL = "label";
    public static final String TICKET_DESCRIPTION = "description";
    public static final String TICKET_PRICE = "price";

    static StringSerializer stringSerializer = StringSerializer.get();
    static UUIDSerializer uuidSerializer = UUIDSerializer.get();
    static ByteBufferSerializer byteBufferSerializer = ByteBufferSerializer.get();

    private ColumnFamilyTemplate<UUID, String> template;
    private Mutator<UUID> mutator;

    public TicketDao(Cluster cluster, Keyspace keyspace) {
        this.cluster = cluster;
        this.keyspace = keyspace;
        this.template =
                new ThriftColumnFamilyTemplate<UUID, String>(keyspace,
                        COLUMN_FAMILY,
                        uuidSerializer,
                        stringSerializer);

        mutator = createMutator(keyspace, uuidSerializer);
    }

    public void create(Ticket ticket) {
        UUID id = UUID.randomUUID();
        mutator.addInsertion(id, COLUMN_FAMILY, createStringColumn(TICKET_LABEL, ticket.getLabel()));
        mutator.addInsertion(id, COLUMN_FAMILY, createStringColumn(TICKET_DESCRIPTION, ticket.getDescription()));
        mutator.addInsertion(id, COLUMN_FAMILY, createColumn(TICKET_PRICE, ticket.getPrice()));

        ticket.setId(id);

        MutationResult mr = mutator.execute();
    }

    public Ticket find(UUID id) {
        Ticket ticket = null;
        try {
            ColumnFamilyResult<UUID, String> res = template.queryColumns(id);
            if (res.hasResults()) {
                ticket = new Ticket();
                ticket.setId(res.getKey());
                ticket.setLabel(res.getString(TICKET_LABEL));
                ticket.setDescription(res.getString(TICKET_DESCRIPTION));
                ticket.setPrice(res.getLong(TICKET_PRICE));
            }
        } catch (HectorException e) {
            System.err.println(e.getMessage());
        }
        return ticket;
    }

    public List<Ticket> all(int count) {
        List<Ticket> ticketList = new ArrayList<Ticket>();
        int row_count = 100;

        RangeSlicesQuery<UUID, String, ByteBuffer> rangeSlicesQuery =
                createRangeSlicesQuery(keyspace, uuidSerializer, stringSerializer, byteBufferSerializer);
        rangeSlicesQuery.setColumnFamily(COLUMN_FAMILY);
//        rangeSlicesQuery.setColumnNames("city","state","lat","lng");
//        rangeSlicesQuery.setKeys("512202", "512205");
        // setRange(N start, N finish, boolean reversed, int count)
        rangeSlicesQuery.setRange(null, null, false, 5);
        rangeSlicesQuery.setRowCount(count);

        QueryResult<OrderedRows<UUID, String, ByteBuffer>> result = rangeSlicesQuery.execute();
        OrderedRows<UUID, String, ByteBuffer> rows = result.get();
        for (Row<UUID, String, ByteBuffer> row : rows) {
            Ticket t = new Ticket();
            t.setId(row.getKey());
            t.setLabel(getString(row.getColumnSlice().getColumnByName(TICKET_LABEL).getValue()));
            t.setDescription(getString(row.getColumnSlice().getColumnByName(TICKET_DESCRIPTION).getValue()));
            t.setPrice(row.getColumnSlice().getColumnByName(TICKET_PRICE).getValue().asLongBuffer().get());
            ticketList.add(t);
        }

        return ticketList;
    }

    public void delete(UUID id) {
        mutator.addDeletion(id, COLUMN_FAMILY, TICKET_LABEL, stringSerializer);
        mutator.addDeletion(id, COLUMN_FAMILY, TICKET_DESCRIPTION, stringSerializer);
        mutator.addDeletion(id, COLUMN_FAMILY, TICKET_PRICE, stringSerializer);
        MutationResult mr = mutator.execute();
        // TODO Necessary?
//        mutator.discardPendingMutations();
    }

    public void update(Ticket ticket) {
        ColumnFamilyUpdater<UUID, String> updater = template.createUpdater(ticket.getId());
        updater.setString(TICKET_LABEL, ticket.getLabel());
        updater.setString(TICKET_LABEL, ticket.getDescription());
        updater.setLong(TICKET_PRICE, ticket.getPrice());
        template.update(updater);
    }

    // *******************************************************
    public static String getString(ByteBuffer byteBuffer) {
        byte[] byteArr = new byte[byteBuffer.remaining()];
        byteBuffer.get(byteArr);
        return new String(byteArr);
    }
}
