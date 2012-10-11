package models;

import java.util.List;
import java.util.UUID;

/**
 * @author Guillaume ALAUX <guillaume at alaux dot net>
 *         Date: 10/11/12
 */
public class ListTicketResult {

    public List<Ticket> tickets;

    public UUID prevId;

    public UUID nextId;

    public ListTicketResult(List<Ticket> tickets, UUID prevId, UUID nextId) {
        this.tickets = tickets;
        this.prevId = prevId;
        this.nextId = nextId;
    }
}
