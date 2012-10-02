package controllers;

import models.Ticket;
import play.data.Form;
import play.mvc.Controller;

/**
 * @author Guillaume ALAUX <guillaume at alaux dot net>
 *         Date: 10/2/12
 */
public class TicketEdit extends Controller {

    static Form<Ticket> ticketForm = form(Ticket.class);


}
