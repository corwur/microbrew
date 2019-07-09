package corwur.microbrew.structure.model;

import java.util.Objects;

public class Sequence {
    public final String name;
    public final String organism;
    public final long length;
    public final long id;

    public Sequence(String name, String organism, long length, long id) {
        this.name = name;
        this.organism = organism;
        this.length = length;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getOrganism() {
        return organism;
    }
    
    public long getLength() {
    	return length; 
    }

    public long getId() {
    	return id;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sequence sequence = (Sequence) o;
        return name.equals(sequence.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
