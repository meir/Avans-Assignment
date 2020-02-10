package nl.michaelmeir.avans.responders;

import com.mysql.cj.xdevapi.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.stream.Collectors;

public abstract class Responder implements HttpHandler {

    protected String root;
    protected String prefix;
    protected Dotenv env;
    protected HashMap<String, String> defaultHeaders;
    protected static String NOT_FOUND = "404 Not found!";

    public Responder(String root, String prefix, Dotenv env, HashMap<String, String> defaultHeaders) {
        this.root = root;
        this.prefix = prefix;
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
                        if(body != null) {
                            response = method.invoke(this, t, body);
                        }else{
                            response = method.invoke(this, t);
                        }
                        if(response instanceof String) {
                            this.close(t, (String) response);
                        }else if(response instanceof byte[]) {
                            this.closeRaw(t, (byte[]) response);
                        }else if(response instanceof ResultSet) {
                            this.close(t, resultSetToJson((ResultSet) response).toString());
                        }else if(response instanceof JSONObject) {
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

    public static JSONArray resultSetToJson(ResultSet resultSet) {
        JSONArray array = new JSONArray();
        try {
            if (!resultSet.isBeforeFirst()) {
                return array;
            }

            while (resultSet.next()) {
                JSONObject jsonObject = new JSONObject();
                for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                    jsonObject.put(resultSet.getMetaData().getColumnName(i), resultSet.getObject(i));
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
