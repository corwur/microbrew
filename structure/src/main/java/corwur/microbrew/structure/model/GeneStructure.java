package corwur.microbrew.structure.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class GeneStructure {
    public final Collection<Gene> genes;
    public final Collection<Organism> organisms;
    public final Collection<Sequence> sequences;
    public final Collection<Order> order;
    public final Collection<Backbone> backbone;

    public GeneStructure(Collection<Gene> genes, Collection<Organism> organisms, Collection<Sequence> sequences, Collection<Order> order, Collection<Backbone> backbone) {
        this.genes = genes;
        this.organisms = organisms;
        this.sequences = sequences;
        this.order = order;
        this.backbone = backbone;
    }
}
