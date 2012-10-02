package models;

import play.data.format.Formats;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author Guillaume ALAUX <guillaume at alaux dot net>
 *         Date: 10/1/12
 */
@Entity
public class Ticket extends Model {

    public static Finder<Long, Ticket> find = new Finder(Long.class, Ticket.class);

    @Id
    public Long id;

    @Required
    public String label;

    public String description;

    @Required
    public BigDecimal price;

    @Required
    @Formats.DateTime(pattern = "dd/MM/yyyy")
    public Date start;

    @Required
    @Formats.DateTime(pattern = "dd/MM/yyyy")
    public Date end;

    public static void create(Ticket ticket) {
        ticket.save();
    }

    public static List<Ticket> all() {
        return find.all();
    }

    public static void delete(Long id) {
        find.ref(id).delete();
    }
}
