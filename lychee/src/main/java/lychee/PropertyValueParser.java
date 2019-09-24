package lychee;

import java.util.Map;
import java.util.regex.Pattern;

public class PropertyValueParser {
    private static final Pattern pattern = Pattern.compile("\\$\\{(.*?)(:#\\{(.*?)\\})?\\}");
    private final Map<String, String> env;

    public PropertyValueParser(Map<String, String> env) {
        this.env = env;
    }

    public String parse(String source) {

        var matcher = pattern.matcher(source);
        var result = "";
        var start = 0;
        while (matcher.find()) {
            result += source.substring(start, matcher.start());
            var defaultValue = matcher.group(3) == null ? "" : matcher.group(3);
            result += env.getOrDefault(matcher.group(1),  defaultValue);
            start = matcher.end();
        }
        result += source.substring(start);
        return result;
    }

}
