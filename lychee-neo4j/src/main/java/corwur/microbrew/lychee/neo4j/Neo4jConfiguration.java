package corwur.microbrew.lychee.neo4j;

import lychee.ApplicationConfiguration;

public interface Neo4jConfiguration {
    String getNeo4jUri();

    String getNeo4jUser();

    String getNeo4jPassword();

    default String getNeo4jUri(ApplicationConfiguration configuration) {
        return configuration.getString("neo4j.uri");
    }

    default String getNeo4jUser(ApplicationConfiguration configuration) {
        return configuration.getString("neo4j.user");
    }
    default String getNeo4jPassword(ApplicationConfiguration configuration) {
        return configuration.getString("neo4j.password");
    }

}
