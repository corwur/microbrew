package lychee;

import com.sun.net.httpserver.HttpExchange;

class Route {

    private final Method method;
    private final RequestMatcher requestMatcher;
    private final Handler handler;

    Route(Method method, RequestMatcher requestMatcher, Handler handler) {
        this.method = method;
        this.requestMatcher = requestMatcher;
        this.handler = handler;
    }

    boolean match(HttpExchange exchange) {
        return method.is(exchange.getRequestMethod()) && requestMatcher.matches(exchange);
    }

    Handler getHandler() {
        return handler;
    }

    Request getRequest(HttpExchange exchange) {
        return requestMatcher.create(exchange);
    }
}
