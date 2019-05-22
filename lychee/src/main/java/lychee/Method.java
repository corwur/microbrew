package lychee;

public enum Method {
    GET,
    POST,
    PUT,
    DELETE;

    public boolean is(String requestMethod) {
        return name().equalsIgnoreCase(requestMethod);
    }
}
