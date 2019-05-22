package lychee;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Request {

    private final HttpExchange exchange;
    private final Map<String, List<String>> queryParameters;
    private final RequestPath requestPath;

    static Request create(HttpExchange exchange, RequestPath requestPath) {
        return new Request(exchange, requestPath, getQueryParameters(exchange));
    }

    Request(HttpExchange exchange, RequestPath requestPath, Map<String, List<String>> queryParameters) {
        this.exchange = exchange;
        this.queryParameters = queryParameters;
        this.requestPath = requestPath;
    }

    public Optional<String> get(String name) {

        if(requestPath.containsKey(name)) {
            return Optional.ofNullable(requestPath.get(name));
        }
        if(queryParameters.containsKey(name)) {
            return queryParameters.get(name).stream().findFirst();
        }

        return Optional.empty();
    }

    public List<String> getAll(String name) {

        if(requestPath.containsKey(name)) {
            return Optional.ofNullable(requestPath.get(name)).map(Arrays::asList).orElse(Collections.emptyList());
        }
        if(queryParameters.containsKey(name)) {
            return queryParameters.get(name);
        }

        return Collections.emptyList();
    }


    private static Map<String, List<String>> getQueryParameters(HttpExchange exchange) {
        return Stream.of(exchange.getRequestURI().getQuery())
                .filter(query -> query != null)
                .flatMap(query -> Stream.of(query.split("&")))
                .map(KeyValuePair::parse)
                .map(KeyValuePair::decode)
                .collect(Collectors.toMap(KeyValuePair::getKey, Request::getValueAsList, Request::mergeLists));
    }

    private static List<String> mergeLists(List<String> u1, List<String> u2) {
        return Stream.concat(u1.stream(), u2.stream()).collect(Collectors.toList());
    }

    private static List<String> getValueAsList(KeyValuePair keyValuePair) {
        return Arrays.asList(keyValuePair.getValue());
    }

    public String getBody() throws IOException {
        InputStream inputStream = this.exchange.getRequestBody();
        return new String(inputStream.readAllBytes(), Charset.defaultCharset());
    }
}
