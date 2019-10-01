package lychee;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApplicationConfiguration {

    private final Properties properties;

    private ApplicationConfiguration(Properties properties) {
        this.properties = properties;
    }

    public static ApplicationConfiguration  load(File file) throws IOException {
        return load(new FileInputStream(file));
    }

    public static ApplicationConfiguration  load(String resourceName) throws IOException {
        return load(Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName));
    }

    public static ApplicationConfiguration  load(InputStream inputStream) throws IOException {
        Properties properties = new Properties();
        properties.load(inputStream);
        var propertyValueParser = new PropertyValueParser(System.getenv());
        properties.replaceAll((key, value) -> propertyValueParser.parse(value.toString()));

        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration(properties);
        return applicationConfiguration;
    }

    public int getInteger(String property) {
        return Integer.parseInt(properties.getProperty(property));
    }

    public String getString(String property) {
        return properties.getProperty(property);
    }
}
