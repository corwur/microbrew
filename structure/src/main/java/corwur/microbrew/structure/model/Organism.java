package corwur.microbrew.structure.model;

public class Organism {
    public final String name;
    public final long id;
    
    public Organism(String name, long id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }
    
    public long getId() {
    	return id;
    }
}
