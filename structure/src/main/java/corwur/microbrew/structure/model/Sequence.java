package corwur.microbrew.structure.model;

import java.util.Objects;

public class Sequence {
    public final String name;
    public final String organism;

    public Sequence(String name, String organism) {
        this.name = name;
        this.organism = organism;
    }

    public String getName() {
        return name;
    }

    public String getOrganism() {
        return organism;
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
