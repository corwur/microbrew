package lychee;


import java.net.URLDecoder;
import java.nio.charset.Charset;

class KeyValuePair {

    private final String key;
    private final String value;

    private KeyValuePair(String key, String value) {
        this.key = key;
        this.value = value;
    }

    KeyValuePair decode () {
        return new KeyValuePair(
                URLDecoder.decode(key, Charset.forName("UTF-8")),
                URLDecoder.decode(value, Charset.forName("UTF-8"))
        );
    }
    String getKey() {
        return key;
    }

    String getValue() {
        return value;
    }

    static KeyValuePair parse(String param) {
        String[] data = param.split("=");
        if(data.length != 2) {
            throw new IllegalArgumentException("Could not parse param");
        }
        return new KeyValuePair(data[0], data[1]);
    }
}
