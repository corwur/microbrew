package corwur.microbrew.pathway;

public class ReactomeClientProvider {

    private final PathwayConfiguration pathwayConfiguration;

    public ReactomeClientProvider(PathwayConfiguration pathwayConfiguration) {
        this.pathwayConfiguration = pathwayConfiguration;
    }

    ReactomeClient get() {
        return new ReactomeClient(pathwayConfiguration.getReactomeNeo4jUrl(), pathwayConfiguration.getReactomeNeo4jUsername(), pathwayConfiguration.getReactomeNeo4jPassword());
    }
}
