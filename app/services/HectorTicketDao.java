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
import me.prettyprint.hector.api.factory.HFactory;
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
public class HectorTicketDao implements TicketDao {

    private Cluster cluster;
    private Keyspace keyspace;

    static StringSerializer stringSerializer = StringSerializer.get();
    static UUIDSerializer uuidSerializer = UUIDSerializer.get();
    static ByteBufferSerializer byteBufferSerializer = ByteBufferSerializer.get();

    private ColumnFamilyTemplate<UUID, String> template;
    private Mutator<UUID> mutator;

    public HectorTicketDao(String clusterName, String clusterHost, int clusterPort, String keyspaceName) {
        cluster = getOrCreateCluster(clusterName, clusterHost + ":" + clusterPort);
        keyspace = HFactory.createKeyspace(keyspaceName, cluster);

        this.template =
                new ThriftColumnFamilyTemplate<UUID, String>(keyspace,
                        CF_TICKET_NAME,
                        uuidSerializer,
                        stringSerializer);

        mutator = createMutator(keyspace, uuidSerializer);
    }

    public void create(Ticket ticket) {
        UUID id = UUID.randomUUID();
        mutator.addInsertion(id, CF_TICKET_NAME, createStringColumn(TICKET_LABEL, ticket.getLabel()));
        mutator.addInsertion(id, CF_TICKET_NAME, createStringColumn(TICKET_DESCRIPTION, ticket.getDescription()));
        mutator.addInsertion(id, CF_TICKET_NAME, createColumn(TICKET_PRICE, ticket.getPrice()));

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

    public List<Ticket> all() {

        List<Ticket> ticketList = new ArrayList<Ticket>();

        RangeSlicesQuery<UUID, String, ByteBuffer> rangeSlicesQuery =
                createRangeSlicesQuery(keyspace, uuidSerializer, stringSerializer, byteBufferSerializer);
        rangeSlicesQuery.setColumnFamily(CF_TICKET_NAME);
        rangeSlicesQuery.setColumnNames(TICKET_LABEL, TICKET_DESCRIPTION, TICKET_PRICE);
        rangeSlicesQuery.setRange(null, null, false, 5);
        rangeSlicesQuery.setKeys(null, null);

        QueryResult<OrderedRows<UUID, String, ByteBuffer>> result = rangeSlicesQuery.execute();
        OrderedRows<UUID, String, ByteBuffer> rows = result.get();

        for (Row<UUID, String, ByteBuffer> row : rows) {
            // Check for "tombstone" rows
            // http://wiki.apache.org/cassandra/FAQ#range_ghosts
            // http://stackoverflow.com/questions/7341439/hector-cassandra-delete-anomaly
            if (!row.getColumnSlice().getColumns().isEmpty()) {
                Ticket t = new Ticket();
                t.setId(row.getKey());
                t.setLabel(getString(row.getColumnSlice().getColumnByName(TICKET_LABEL).getValue()));
                t.setDescription(getString(row.getColumnSlice().getColumnByName(TICKET_DESCRIPTION).getValue()));
                t.setPrice(row.getColumnSlice().getColumnByName(TICKET_PRICE).getValue().asLongBuffer().get());
                ticketList.add(t);
            }
        }

        return ticketList;
    }

    /**
     * @param pageSize
     * @param id
     * @return
     */
    public List<Ticket> getPage(int pageSize, boolean reversed, UUID id) {

        List<Ticket> tickets = new ArrayList<Ticket>();

        RangeSlicesQuery<UUID, String, ByteBuffer> rangeSlicesQuery =
                createRangeSlicesQuery(keyspace, uuidSerializer, stringSerializer, byteBufferSerializer);
        rangeSlicesQuery.setColumnFamily(CF_TICKET_NAME);
        rangeSlicesQuery.setColumnNames(TICKET_LABEL, TICKET_DESCRIPTION, TICKET_PRICE);
        rangeSlicesQuery.setRange(null, null, reversed, 5);
        rangeSlicesQuery.setKeys((id == HectorTicketDao.UUID_NULL) ? null : id, null);
        // Let's ask for one more ticket and adjust the result afterwards
        rangeSlicesQuery.setRowCount(pageSize + 1);

        QueryResult<OrderedRows<UUID, String, ByteBuffer>> result = rangeSlicesQuery.execute();
        OrderedRows<UUID, String, ByteBuffer> rows = result.get();

        // FIXME: we ask for "count" elements but if we encounter a tombstone then we will send "count - 1" elements
        for (Row<UUID, String, ByteBuffer> row : rows) {
            // Check for "tombstone" rows
            // http://wiki.apache.org/cassandra/FAQ#range_ghosts
            // http://stackoverflow.com/questions/7341439/hector-cassandra-delete-anomaly
            if (!row.getColumnSlice().getColumns().isEmpty()) {
                Ticket t = new Ticket();
                t.setId(row.getKey());
                t.setLabel(getString(row.getColumnSlice().getColumnByName(TICKET_LABEL).getValue()));
                t.setDescription(getString(row.getColumnSlice().getColumnByName(TICKET_DESCRIPTION).getValue()));
                t.setPrice(row.getColumnSlice().getColumnByName(TICKET_PRICE).getValue().asLongBuffer().get());
                tickets.add(t);
            }
        }

        if (!tickets.isEmpty()) {
            tickets.remove(0);
        }

        return tickets;
    }

    public void delete(UUID id) {
        mutator.addDeletion(id, CF_TICKET_NAME, TICKET_LABEL, stringSerializer);
        mutator.addDeletion(id, CF_TICKET_NAME, TICKET_DESCRIPTION, stringSerializer);
        mutator.addDeletion(id, CF_TICKET_NAME, TICKET_PRICE, stringSerializer);
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
