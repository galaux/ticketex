package models;

import services.HectorTicketDao;

import java.util.List;
import java.util.UUID;

/**
 * @author Guillaume ALAUX <guillaume at alaux dot net>
 *         Date: 10/11/12
 */
public class ListTicketResult {

    public List<Ticket> tickets;

    public UUID prevPageFirstId = HectorTicketDao.UUID_NULL;

    public UUID nextPageFirstId = HectorTicketDao.UUID_NULL;
}
