package lychee;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.function.Supplier;

class ContextHandler implements HttpHandler {

    private Collection<Route> routes = new ArrayList<>();
    private Handler defaultHandler = (req, res) -> res.notFound("Not found");
    private ExceptionHandler exceptionHandler = (exception, req, res) -> res.internalServerError(exception.getMessage());;
    private final ResponseFactory responseFactory;

    ContextHandler(ResponseFactory responseFactory) {
        this.responseFactory = responseFactory;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        var route = routes.stream().filter(match(exchange)).findFirst();
        var handler = route.map(Route::getHandler).orElse(defaultHandler);
        var request = route.map(r -> r.getRequest(exchange)).orElse(getDefaultRequest(exchange));
        var response = responseFactory.create(exchange);
        try {
            handler.handle(request, response);
        } catch (Exception e) {
            exceptionHandler.handle(e, request, response);
        }
    }

    private Request getDefaultRequest(HttpExchange exchange) {
        return Request.create(exchange, new PlainRequestPath(exchange.getRequestURI().getPath()));
    }

    private Predicate<? super Route> match(HttpExchange exchange) {
        return route -> route.match(exchange);
    }

    public void addRoute(Route route) {
        routes.add(route);
    }

    public void setDefaultHandler(Handler defaultHandler) {
        this.defaultHandler = defaultHandler;
    }

    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }
}
