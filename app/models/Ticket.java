package models;

import play.data.format.Formats;

import java.util.Date;
import java.util.UUID;

/**
 * @author Guillaume ALAUX <guillaume at alaux dot net>
 *         Date: 10/3/12
 */
public class Ticket {

    public UUID id;

    public String label;

    public String description;

    public Long price;

    @Formats.DateTime(pattern="dd/MM/yyyy")
    public Date start;

    @Formats.DateTime(pattern="dd/MM/yyyy")
    public Date end;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    @Override
    public String toString() {
        return "Ticket[" + id + "]:" + "\n"
                + "\tlabel\t\t: " + label + "\n"
                + "\tdescription\t: " + description + "\n"
                + "\tprice\t\t: €" + price;
    }
}