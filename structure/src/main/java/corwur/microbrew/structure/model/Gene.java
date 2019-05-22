package corwur.microbrew.structure.model;

import java.util.ArrayList;
import java.util.List;

public class Gene {
    public final String geneIdentifier;
    public List<On> on = new ArrayList<>();

    public Gene(String geneIdentifier) {
        this.geneIdentifier = geneIdentifier;
    }

    public String getGeneIdentifier() {
        return geneIdentifier;
    }

    public void addOn(On on) {
        this.on.add(on);
    }

}
