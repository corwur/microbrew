package corwur.microbrew.structure.model;

public class Backbone {

    public final long from;
    public final long to;
    public final long of;
    public final long id;
    

	public Backbone(long from, long to, long id, long of) {
        this.from = from;
        this.to = to;
        this.of = of;
        this.id = id;
    }

    public long getFrom() {
        return from;
    }

    public long getTo() {
        return to;
    }
    
    public long getOf() {
    	return of;
    }
    public long getId() {
		return id;
	}
}
