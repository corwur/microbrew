package corwur.microbrew.structure.model;

import java.util.ArrayList;
import java.util.List;

public class Gene {
    public final String name;
    public List<On> on = new ArrayList<>();
    public final long id;
    
    public Gene(String geneIdentifier, long id) {
        this.name = geneIdentifier;
        this.id = id;
    }

    public String getGeneIdentifier() {
        return name;
    }
    
    public long getId() {
    	return id;
    }

    public void addOn(On on) {
        this.on.add(on);
    }

}
