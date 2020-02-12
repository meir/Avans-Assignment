package nl.michaelmeir.avans.responders;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.util.HashMap;

//Responder is an abstract HttpHandler class that will handle the requests and return the response given by the corresponding method
public abstract class Responder implements HttpHandler {

    protected String root; //root is the location of the web files
    protected String prefix; //prefix is the prefix of the url for example: / or /api/
    protected Dotenv env; //env is the .env file containing important data
    protected HashMap<String, String> defaultHeaders; //defaultHeaders containers all default headers that need to be added to any response
    protected static String NOT_FOUND = "404 Not found!"; //NOT_FOUND is an constant for 404 responses

    //Responder requires root, prefix, env and defaultHeaders to store for later use
    public Responder(String root, String prefix, Dotenv env, HashMap<String, String> defaultHeaders) {
        this.root = root;
        this.prefix = prefix;
        this.env = env;
        this.defaultHeaders = defaultHeaders;
    }

    //handle handles all incoming http requests.
    //using reflect it will find a method corresponding to the request and call it for a response.
    //the response will be turned into a string if supported and sent back to the client
    //
    //Examples:
    //  url: /api/performances/get
    //  method: performances_get
    //
    //  url: /
    //  method: index
    //
    //  url: /specific/path/to/call
    //  method: specific_path_to_call
    //
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
                try {
                    method = this.getClass().getMethod(methodName, HttpExchange.class, JSONObject.class);
                }catch(NoSuchMethodException e) {
                    method = this.getClass().getMethod(methodName, HttpExchange.class);
                }
            }catch(NoSuchMethodException e) {
                method = this.getClass().getMethod("index", HttpExchange.class);
            }
            if(method != null) {
                RequestMethod requestMethod = method.getAnnotation(RequestMethod.class);
                if(requestMethod.value().equals(t.getRequestMethod())) {
                    JSONObject body = this.getBody(t);
                    if(this.bodyRequirementsMet(t, method, body)) {
                        Object response;
                        if(body != null && body.length() > 0) {
                            response = method.invoke(this, t, body);
                        }else{
                            response = method.invoke(this, t);
                        }
                        if(response == null) {
                            this.close(t, "{\"success\":false}");
                        }else if(response instanceof String) {
                            this.close(t, (String) response);
                        }else if(response instanceof byte[]) {
                            this.closeRaw(t, (byte[]) response);
                        }else if(response instanceof ResultSet) {
                            this.close(t, resultSetToJson((ResultSet) response).toString());
                        }else if(response instanceof JSONObject) {
                            this.close(t, response.toString());
                        }else if(response instanceof JSONArray) {
                            this.close(t, response.toString());
                        }else{
                            System.out.println("Unsupported type has been returned by handler method: " + methodName);
                            this.close(t, "Handler call failed!");
                        }
                    }else{
                        this.close(t, "Body was not valid");
                    }
                }else{
                    this.close(t, "Expected to be requested as a " + requestMethod.value() + " request");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Method not found!");
            this.noneFound(t);
        }
    }

    //noneFound will return the NOT_FOUND response to the client
    public void noneFound(HttpExchange t) throws IOException {
        this.close(t, NOT_FOUND);
    }

    //close will close the request by sending the response string given
    public void close(HttpExchange t, String response) throws IOException {
        this.closeRaw(t, response.getBytes());
    }

    //closeRaw will close the request by sending the raw byte array given
    public void closeRaw(HttpExchange t, byte[] response) throws IOException {
        t.sendResponseHeaders(200, response.length);
        OutputStream os = t.getResponseBody();
        os.write(response);
        os.close();
    }

    //bodyRequirementsMet will check if the requirements given above a method using annotations corresponds to a request body
    public boolean bodyRequirementsMet(HttpExchange t, Method method, JSONObject bodyObject) {
        try{
            Required required = method.getAnnotation(Required.class);
            if(required != null) {
                for(Value val : required.value()) {
                    if(bodyObject.has(val.name())) {
                        Object obj = bodyObject.get(val.name());
                        if(obj.getClass() != val.type()) {
                            return false;
                        }
                    }else{
                        return false;
                    }
                }
            }
            return true;
        }catch(Exception e) {
            return true;
        }
    }

    //getBody will return a JSONObject containing the data of the request body
    public JSONObject getBody(HttpExchange t) {
        try{
            InputStreamReader streamReader = new InputStreamReader(t.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(streamReader);

            int b;
            StringBuilder stringBuilder = new StringBuilder(512);
            while ((b = bufferedReader.read()) != -1) {
                stringBuilder.append((char) b);
            }
            bufferedReader.close();
            streamReader.close();

            String body = stringBuilder.toString();
            if(body.length() > 0) {
                return new JSONObject(body);
            }
            return new JSONObject();
        }catch(Exception e) {
            return new JSONObject();
        }
    }

    //resultSetToJson will turn an sql ResultSet to a JSONArray object
    public static JSONArray resultSetToJson(ResultSet resultSet) {
        JSONArray array = new JSONArray();
        try {
            if (!resultSet.isBeforeFirst()) {
                return array;
            }

            while (resultSet.next()) {
                JSONObject jsonObject = new JSONObject();
                for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                    if (resultSet.getObject(i) instanceof String) {
                        String json = (String) resultSet.getObject(i);
                        try{
                            jsonObject.put(resultSet.getMetaData().getColumnName(i), new JSONObject(json));
                        }catch(Exception e) {
                            jsonObject.put(resultSet.getMetaData().getColumnName(i), resultSet.getObject(i));
                        }
                    } else {
                        jsonObject.put(resultSet.getMetaData().getColumnName(i), resultSet.getObject(i));
                    }
                }
                array.put(array.length(), jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONArray();
        }
        return array;
    }
}
