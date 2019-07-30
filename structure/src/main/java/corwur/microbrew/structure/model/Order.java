package corwur.microbrew.structure.model;

public class Order {
    public final long from;
    public final long to;
    public final long id;
    public final String organism;

    public Order(long from, long to, long id, String organism) {
        this.from = from;
        this.to = to;
        this.id = id;
        this.organism = organism;
    }

    public long getFrom() {
        return from;
    }

    public long getTo() {
        return to;
    }

    public long getId() {
    	return id;
    }
    public String getOrganism() {
        return organism;
    }
}
