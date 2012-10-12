package services;

import models.Ticket;

import java.util.List;
import java.util.UUID;

/**
 * @author Guillaume ALAUX <guillaume at alaux dot net>
 *         Date: 10/12/12
 */
public interface TicketDao {

    public static final String CLUSTER_NAME = "cluster_ticketex";
    public static final String KEYSPACE_NAME = "ticketex";

    public static final String CF_TICKET_NAME = "Ticket";
    public static final String TICKET_LABEL = "label";
    public static final String TICKET_DESCRIPTION = "description";
    public static final String TICKET_PRICE = "price";

    public static final UUID UUID_NULL = UUID.fromString("0-0-0-0-0");

    public Ticket find(UUID id);

    public void create(Ticket ticket);

    public List<Ticket> all();

//    public List<Ticket> getPage(int pageSize, boolean reversed, UUID id);

    public void delete(UUID id);

    public void update(Ticket ticket);
}
