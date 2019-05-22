package corwur.microbrew.structure.model;

public class Order {
    public final String from;
    public final String to;
    public final String organism;

    public Order(String from, String to, String organism) {
        this.from = from;
        this.to = to;
        this.organism = organism;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getOrganism() {
        return organism;
    }
}
