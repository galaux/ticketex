package controllers;

import models.Ticket;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller {

    static Form<Ticket> ticketForm = form(Ticket.class);

    public static Result index() {
        // return ok(views.html.index.render("Your new application is ready."));
        return ok(views.html.index.render(Ticket.all(), ticketForm));
    }

    public static Result tickets() {
        return ok(views.html.index.render(Ticket.all(), ticketForm));
    }

    public static Result newTicket() {
        Form<Ticket> filledForm = ticketForm.bindFromRequest();
        if (filledForm.hasErrors()) {
            return badRequest(views.html.index.render(Ticket.all(), filledForm));
        } else {
            Ticket.create(filledForm.get());
            return redirect(routes.Application.tickets());
        }
    }

    public static Result deleteTicket(Long id) {
        Ticket.delete(id);
        return redirect(routes.Application.tickets());
    }
}