package nl.michaelmeir.avans.responders;

import io.github.cdimascio.dotenv.Dotenv;
import nl.michaelmeir.avans.query.CreateTable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import com.sun.net.httpserver.HttpExchange;

import org.json.JSONArray;
import org.json.JSONObject;

public class API extends Responder {

    private Connection connection;

    public API(Dotenv env) {
        super("web", "/api/", env, new HashMap<String, String>() {
            {
                put("Access-Control-Allow-Origin", "*");
                put("Content-Type", "application/json");
            }
        });
        try{
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            String host = env.get("DB_HOST", "localhost");
            String port = env.get("DB_PORT", "3306");
            String db = env.get("DB_NAME", "avans");
            String user = env.get("DB_USER", "root");
            String pass = env.get("DB_PASS", "password");
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + db + "?user=" + user + "&password=" + pass);

            new CreateTable(connection, "artists", new HashMap<String, String>() {{
                put("name", "varchar(255) NOT NULL");
                put("description", "TEXT NOT NULL");
                put("type", "varchar(64) NOT NULL");
            }});
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMethod("GET")
    public ResultSet artists_get(HttpExchange t) {
        try{
            Statement statement = connection.createStatement();
            ResultSet results =  statement.executeQuery("SELECT * FROM artists LIMIT 10");
            return results;
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @RequestMethod("POST")
    @Required({
            @Value(name="query", type=String.class)
    })
    public ResultSet artists_search(HttpExchange t, JSONObject body) {
        try{
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM artists WHERE name LIKE ?;");
            statement.setString(1, "%" + body.getString("query") + "%");
            return statement.executeQuery();
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @RequestMethod("POST")
    @Required({
            @Value(name="id", type=Integer.class)
    })
    public Object artist_get(HttpExchange t, JSONObject body) {
        try{
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM artists WHERE id = ?;");
            statement.setInt(1, body.getInt("id"));
            JSONArray array = resultSetToJson(statement.executeQuery());
            if(array.length() > 0) {
                return array.getJSONObject(0);
            }
            return new JSONObject();
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @RequestMethod("POST")
    @Required({
        @Value(name="name", type=String.class),
        @Value(name="description", type=String.class),
        @Value(name="type", type=String.class)
    })
    public String artists_create(HttpExchange t, JSONObject body) {
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO artists(name, description, type) VALUES(?, ?, ?)");
            statement.setString(1, body.getString("name"));
            statement.setString(2, body.getString("description"));
            statement.setString(3, body.getString("type"));
            statement.execute();
            return "{\"success\":true}";
        }catch(Exception e) {
            e.printStackTrace();
            return "{\"success\":false}";
        }
    }
}
