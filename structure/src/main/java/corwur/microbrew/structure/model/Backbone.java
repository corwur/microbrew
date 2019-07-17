package corwur.microbrew.structure.model;

public class Backbone {

    public final String from;
    public final String to;
    public final long of;
    
    public Backbone(String from, String to, long of) {
        this.from = from;
        this.to = to;
        this.of = of;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }
    
    public long getOf() {
    	return of;
    }
}
