package controllers;

import models.Ticket;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller {

    static Form<Ticket> ticketForm = form(Ticket.class);

    public static final Result GO_HOME = redirect(routes.Application.list());

    public static Result index() {
        return GO_HOME;
    }

    public static Result list() {
        return ok(views.html.list.render(Ticket.all()));
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
            return badRequest(views.html.list.render(Ticket.all()));
        } else {
            Ticket.create(filledForm.get());
            return redirect(routes.Application.list());
        }
    }

    public static Result onDoDeleteClick(Long id) {
        Ticket.delete(id);
        return redirect(routes.Application.list());
    }
}