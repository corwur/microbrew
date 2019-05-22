package corwur.microbrew.structure;

import java.io.IOException;
import java.util.Properties;

public class ApplicationConfiguration {
    private String neo4jUri;
    private String neo4jUser;
    private String neo4jPassword;
    private int port;

    public static ApplicationConfiguration load(String resourceName) throws IOException {

        Properties properties = new Properties();
        properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName));

        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration();
        applicationConfiguration.setPort(Integer.parseInt(properties.getProperty("server.port")));
        applicationConfiguration.setNeo4jUri(properties.getProperty("neo4j.uri"));
        applicationConfiguration.setNeo4jUser(properties.getProperty("neo4j.user"));
        applicationConfiguration.setNeo4jPassword(properties.getProperty("neo4j.password"));
        return applicationConfiguration;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getNeo4jUri() {
        return neo4jUri;
    }

    public void setNeo4jUri(String neo4jUri) {
        this.neo4jUri = neo4jUri;
    }

    public String getNeo4jUser() {
        return neo4jUser;
    }

    public void setNeo4jUser(String neo4jUser) {
        this.neo4jUser = neo4jUser;
    }

    public String getNeo4jPassword() {
        return neo4jPassword;
    }

    public void setNeo4jPassword(String neo4jPassword) {
        this.neo4jPassword = neo4jPassword;
    }
}
