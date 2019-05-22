package corwur.microbrew.structure.model;

public class Backbone {

    public final String from;
    public final String to;

    public Backbone(String from, String to) {
        this.from = from;
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }
}
