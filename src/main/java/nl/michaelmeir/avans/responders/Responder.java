package nl.michaelmeir.avans.responders;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.HashMap;

public abstract class Responder implements HttpHandler {

    protected String root;
    protected String prefix;
    protected Dotenv env;
    protected HashMap<String, String> defaultHeaders;
    protected static String NOT_FOUND = "404 Not found!";

    public Responder(String root, String prefix, Dotenv env, HashMap<String, String> defaultHeaders) {
        this.root = root;
        this.prefix = "prefix";
        this.env = env;
        this.defaultHeaders = defaultHeaders;
    }

    public void handle(HttpExchange t) throws IOException {
        for(String key : this.defaultHeaders.keySet()) {
            t.getResponseHeaders().add(key, this.defaultHeaders.get(key));
        }
        String path = t.getRequestURI().getRawPath();
        String methodName = path.replaceFirst(prefix, "").replaceAll("/", "_");
        System.out.println(t.getRequestMethod() + " > " + path + " - " + methodName);
        try {
            Method method;
            try {
                method = this.getClass().getMethod(methodName, HttpExchange.class);
            }catch(NoSuchMethodException e) {
                method = this.getClass().getMethod("index", HttpExchange.class);
            }
            if(method != null) {
                RequestMethod requestMethod = method.getAnnotation(RequestMethod.class);
                if(requestMethod.value().equals(t.getRequestMethod())) {
                    Object response = method.invoke(this, t);
                    if(response instanceof String) {
                        this.close(t, (String) response);
                    }else if(response instanceof byte[]) {
                        this.closeRaw(t, (byte[]) response);
                    }else{
                        System.out.println("Unsupported type has been returned by handler method: " + methodName);
                        this.close(t, "Handler call failed!");
                    }
                }
            }
        } catch (Exception e) {
            this.noneFound(t);
        }
    }

    public void noneFound(HttpExchange t) throws IOException {
        this.close(t, NOT_FOUND);
    }

    public void close(HttpExchange t, String response) throws IOException {
        this.closeRaw(t, response.getBytes());
    }

    public void closeRaw(HttpExchange t, byte[] response) throws IOException {
        t.sendResponseHeaders(200, response.length);
        OutputStream os = t.getResponseBody();
        os.write(response);
        os.close();
    }
}
