package corwur.microbrew.structure.model;

public class On {
    public final long geneID;
    public final long sequenceID;
    public final long start;
    public final long end;
    public final long id;

    public On(long geneID, long sequenceID, long id, long start, long end) {
        this.geneID = geneID;
        this.sequenceID = sequenceID;
        this.start = start;
        this.end = end;
        this.id = id;
    }
}
