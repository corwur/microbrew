package corwur.microbrew.structure.model;

public class On {
    public final String gene;
    public final String sequence;
    public final long start;
    public final long end;
    public final long id;

    public On(String gene, String sequence, long id, long start, long end) {
        this.gene = gene;
        this.sequence = sequence;
        this.start = start;
        this.end = end;
        this.id = id;
    }
}
