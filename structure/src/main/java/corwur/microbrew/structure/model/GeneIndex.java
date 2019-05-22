package corwur.microbrew.structure.model;

import java.util.Collection;
import java.util.List;

public class GeneIndex {
    public final Collection<String> genes;
    public final long count;

    public GeneIndex(Collection<String> genes, long count) {
        this.genes = genes;
        this.count = count;
    }
}
