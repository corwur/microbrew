package lychee;

import com.sun.net.httpserver.HttpContext;

public class Context {

    private final HttpContext httpContext;
    private final ContextHandler contextHandler;

    Context(HttpContext httpContext, ContextHandler contextHandler) {
        this.httpContext = httpContext;
        this.contextHandler = contextHandler;
        this.httpContext.setHandler(contextHandler);
    }

    public void get(RequestMatcher requestMatcher, Handler handler) {
        handle(Method.GET, requestMatcher, handler);
    }

    public void post(RequestMatcher requestMatcher, Handler handler) {
        handle(Method.POST, requestMatcher, handler);
    }

    public void put(RequestMatcher requestMatcher, Handler handler) {
        handle(Method.PUT, requestMatcher, handler);
    }

    public void delete(RequestMatcher requestMatcher, Handler handler) {
        handle(Method.DELETE, requestMatcher, handler);
    }

    private void handle(Method method, RequestMatcher requestMatcher, Handler handler) {
        contextHandler.addRoute(new Route(method, requestMatcher, handler));
    }

}
