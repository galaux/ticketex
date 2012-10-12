package controllers;

import com.google.common.base.Strings;
import models.Ticket;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import services.HectorTicketDao;
import services.TicketDao;

import java.util.List;
import java.util.UUID;

public class Application extends Controller {

    public static final String CLUSTER_NAME = "cluster_ticketex";
    public static final String KEYSPACE_NAME = "ticketex";
    public static final String CLUSTER_HOST = "127.0.0.1";
    public static final int CLUSTER_PORT = 9160;

    static TicketDao ticketDao = new HectorTicketDao(CLUSTER_NAME, CLUSTER_HOST, CLUSTER_PORT, KEYSPACE_NAME);
//    static TicketDao ticketDao = new AstyanaxTicketDao(CLUSTER_NAME, CLUSTER_HOST, CLUSTER_PORT, KEYSPACE_NAME);

    static Form<Ticket> ticketForm = form(Ticket.class);
    static Form<String> queryForm = form(String.class);

    public static final int PAGE_SIZE = 3;
    public static final Result GO_HOME = redirect(routes.Application.list());

    public static Result index() {
        return GO_HOME;
    }

    public static Result list() {
        List<Ticket> tickets = ticketDao.all();
        return ok(views.html.list.render(tickets, queryForm));
    }

    public static Result onDoneCreate() {
        return GO_HOME;
    }

    public static Result onShowCreateClick() {
        return ok(views.html.edit.render(ticketForm));
    }

    public static Result onDoCreateClick() {
        Form<Ticket> filledForm = ticketForm.bindFromRequest();
        if (filledForm.hasErrors()) {
            return badRequest(views.html.list.render(ticketDao.all(), queryForm));
        } else {
            ticketDao.create(filledForm.get());
            return redirect(routes.Application.list());
        }
    }

    public static Result onDoDeleteClick(String uuidAsStr) {
        ticketDao.delete(UUID.fromString(uuidAsStr));
        return redirect(routes.Application.list());
    }

    public static Result onSearchClick() {
        Form<String> filledForm = queryForm.bindFromRequest();
        if (filledForm.hasErrors()) {
            return ok("Error - Got '" + filledForm.get() + "'");
        } else {
//            String query = filledForm.data().get("query");
            return TODO;
        }
    }
}