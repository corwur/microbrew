package corwur.microbrew.structure;

import corwur.microbrew.lychee.neo4j.Neo4jConfiguration;
import lychee.ApplicationConfiguration;
import lychee.ServerConfiguration;

public class StructureConfiguration implements ServerConfiguration, Neo4jConfiguration {

    private final ApplicationConfiguration configuration;

    public StructureConfiguration(ApplicationConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public String getNeo4jUri() {
        return getNeo4jUri(configuration);
    }

    @Override
    public String getNeo4jUser() {
        return getNeo4jUser(configuration);
    }

    @Override
    public String getNeo4jPassword() {
        return getNeo4jPassword(configuration);
    }

    @Override
    public int getPort() {
        return getPort(configuration);
    }
}
