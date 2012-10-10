package controllers;

import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.factory.HFactory;
import models.Ticket;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import services.TicketDao;

import java.util.List;
import java.util.UUID;

import static me.prettyprint.hector.api.factory.HFactory.getOrCreateCluster;

public class Application extends Controller {

    public static final String CLUSTER_NAME = "cluster_ticketex";
    public static final String KEYSPACE_NAME = "ticketex";
    public static final String CLUSTER_URI = "127.0.0.1:9160";

    private static Cluster cluster = getOrCreateCluster(CLUSTER_NAME, CLUSTER_URI);
    private static Keyspace keyspace = HFactory.createKeyspace(KEYSPACE_NAME, cluster);

    static TicketDao ticketDao = new TicketDao(cluster, keyspace);

    static Form<Ticket> ticketForm = form(Ticket.class);
    static Form<String> queryForm = form(String.class);

    public static final Result GO_HOME = redirect(routes.Application.list());
    public static final int ALL_MAX_COUNT = 100;

    public static Result index() {
        return GO_HOME;
    }

    public static Result list() {
        List<Ticket> all = ticketDao.all(ALL_MAX_COUNT);
        return ok(views.html.list.render(all, queryForm));
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
            return badRequest(views.html.list.render(ticketDao.all(ALL_MAX_COUNT), queryForm));
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
            String query = filledForm.data().get("query");
//            return ok(views.html.list.render(Ticket.search(query), queryForm));
            return TODO;
        }
    }
}