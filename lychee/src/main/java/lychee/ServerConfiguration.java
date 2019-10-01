package lychee;

public interface ServerConfiguration {

    int getPort();

    default int getPort(ApplicationConfiguration configuration) {
        return configuration.getInteger("server.port");
    }
}
