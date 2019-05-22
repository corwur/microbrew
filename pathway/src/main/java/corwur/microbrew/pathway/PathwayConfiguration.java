package corwur.microbrew.pathway;

import java.io.IOException;
import java.util.Properties;

public class PathwayConfiguration {
    private Properties properties;

    public PathwayConfiguration(Properties properties) {
        this.properties = properties;
    }

    public static PathwayConfiguration load(String resource) throws IOException {
        Properties properties = new Properties();
        properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(resource));
        return new PathwayConfiguration(properties);
    }

    public String getReactomeNeo4jDirectory() {
        return properties.getProperty("reactome.neo4j.directory");
    }
    public String getReactomeNeo4jUrl() {
        return properties.getProperty("reactome.neo4j.url");
    }
    public String getReactomeNeo4jPassword() {
        return properties.getProperty("reactome.neo4j.password");
    }
    public String getReactomeNeo4jUsername() {
        return properties.getProperty("reactome.neo4j.username");
    }


    public int getPort() {
        return Integer.valueOf(properties.getOrDefault("server.port", 8081).toString());
    }
}
