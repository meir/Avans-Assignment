package nl.michaelmeir.avans.responders;

import io.github.cdimascio.dotenv.Dotenv;
import nl.michaelmeir.avans.query.CreateTable;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

import com.sun.net.httpserver.HttpExchange;

import org.json.JSONArray;
import org.json.JSONObject;

//API contains all api calls, it can be called using http(s)://[host]:[port]/api/[method]
public class API extends Responder {

    //connection contains a connection to the database
    private Connection connection;

    //API initializes the connection to the database and the tables needed
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
                put("description", "text NOT NULL");
                put("type", "varchar(64) NOT NULL");
            }});

            new CreateTable(connection, "podia", new HashMap<String, String>() {{
                put("name", "varchar(255) NOT NULL");
                put("location", "varchar(255) NOT NULL");
                put("description", "text NOT NULL");
            }});

            new CreateTable(connection, "podia_artists", new HashMap<String, String>() {{
                put("podium_id", "int(11) NOT NULL");
                put("artist_id", "int(11) NOT NULL");
                put("start", "datetime NOT NULL");
                put("end", "datetime NOT NULL");
            }},
            "KEY `FK_podium_id` (`podium_id`)",
                "KEY `FK_artist_id` (`artist_id`)",
                "CONSTRAINT `FK_podium_id` FOREIGN KEY (`podium_id`) REFERENCES `podia` (`id`) ON DELETE CASCADE ON UPDATE CASCADE",
                "CONSTRAINT `FK_artist_id` FOREIGN KEY (`artist_id`) REFERENCES `artists` (`id`) ON DELETE CASCADE ON UPDATE CASCADE");
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    //artists_get returns all artists
    @RequestMethod("GET")
    public ResultSet artists_get(HttpExchange t) {
        try{
            Statement statement = connection.createStatement();
            ResultSet results =  statement.executeQuery("SELECT * FROM artists;");
            return results;
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //artists_search requires a "query" string in the body, using this it will search for arists with a similar name
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

    //arist_get is a post request that requires an id of an artist, using this it will return all the data about that artists
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

    //artists_create is used to create a new artist requiring a name, description and type
    @RequestMethod("POST")
    @Required({
        @Value(name="name", type=String.class),
        @Value(name="description", type=String.class),
        @Value(name="type", type=String.class)
    })
    public String artists_create(HttpExchange t, JSONObject body) {
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO artists(name, description, type) VALUES(?, ?, ?);");
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

    //artist_update is used to update an artist, requiring an id, anything else will be checked if included and added if so
    @RequestMethod("POST")
    @Required({
            @Value(name="id", type=Integer.class)
    })
    public String artist_update(HttpExchange t, JSONObject body) {
        try{
            ArrayList<String> updated = new ArrayList<String>();
            ArrayList<Object> values = new ArrayList<Object>();
            if(body.has("name")) {
                updated.add("name = ?");
                values.add(body.get("name"));
            }
            if(body.has("description")) {
                updated.add("description = ?");
                values.add(body.get("description"));
            }
            if(body.has("type")) {
                updated.add("type = ?");
                values.add(body.get("type"));
            }

            PreparedStatement statement = connection.prepareStatement("UPDATE artists SET " + String.join(", ", updated) + " WHERE id = ?;");

            for(int i = 0; i < values.size(); i++) {
                statement.setObject(i+1, values.get(i));
            }
            statement.setInt(values.size()+1, body.getInt("id"));

            statement.execute();
            return "{\"success\":true}";
        }catch(Exception e) {
            e.printStackTrace();
            return "{\"success\":false}";
        }
    }

    //artist_delete will delete a specified artist using its id.
    @RequestMethod("POST")
    @Required({
            @Value(name="id", type=Integer.class)
    })
    public String artist_delete(HttpExchange t, JSONObject body) {
        try{
            PreparedStatement statement = connection.prepareStatement("DELETE FROM artists WHERE id = ?;");
            statement.setInt(1, body.getInt("id"));
            statement.execute();
            return "{\"success\":true}";
        }catch(Exception e) {
            e.printStackTrace();
            return "{\"success\":false}";
        }
    }

    //podia_get will return all podia in the database.
    @RequestMethod("GET")
    public ResultSet podia_get(HttpExchange t) {
        try{
            Statement statement = connection.createStatement();
            ResultSet results =  statement.executeQuery("SELECT * FROM podia;");
            return results;
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //podia_search will search for a podium with a similar name.
    @RequestMethod("POST")
    @Required({
            @Value(name="query", type=String.class)
    })
    public ResultSet podia_search(HttpExchange t, JSONObject body) {
        try{
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM podia WHERE name LIKE ?;");
            statement.setString(1, "%" + body.getString("query") + "%");
            return statement.executeQuery();
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //podium_get will return all the data about a specified podium using its id.
    @RequestMethod("POST")
    @Required({
            @Value(name="id", type=Integer.class)
    })
    public Object podium_get(HttpExchange t, JSONObject body) {
        try{
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM podia WHERE id = ?;");
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

    //podia_create will create a podium using its name, location and description.
    @RequestMethod("POST")
    @Required({
            @Value(name="name", type=String.class),
            @Value(name="location", type=String.class),
            @Value(name="description", type=String.class)
    })
    public String podia_create(HttpExchange t, JSONObject body) {
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO podia(name, location, description) VALUES(?, ?, ?);");
            statement.setString(1, body.getString("name"));
            statement.setString(2, body.getString("location"));
            statement.setString(3, body.getString("description"));
            statement.execute();
            return "{\"success\":true}";
        }catch(Exception e) {
            e.printStackTrace();
            return "{\"success\":false}";
        }
    }

    //podium_update updates a podium requiring its id, everything else included in the body will be checked and updated if given.
    @RequestMethod("POST")
    @Required({
            @Value(name="id", type=Integer.class)
    })
    public String podium_update(HttpExchange t, JSONObject body) {
        try{
            ArrayList<String> updated = new ArrayList<String>();
            ArrayList<Object> values = new ArrayList<Object>();
            if(body.has("name")) {
                updated.add("name = ?");
                values.add(body.get("name"));
            }
            if(body.has("location")) {
                updated.add("location = ?");
                values.add(body.get("location"));
            }
            if(body.has("description")) {
                updated.add("description = ?");
                values.add(body.get("description"));
            }

            PreparedStatement statement = connection.prepareStatement("UPDATE podia SET " + String.join(", ", updated) + " WHERE id = ?;");

            for(int i = 0; i < values.size(); i++) {
                statement.setObject(i+1, values.get(i));
            }
            statement.setInt(values.size()+1, body.getInt("id"));

            statement.execute();
            return "{\"success\":true}";
        }catch(Exception e) {
            e.printStackTrace();
            return "{\"success\":false}";
        }
    }

    //podium_delete will delete a podium using its id.
    @RequestMethod("POST")
    @Required({
            @Value(name="id", type=Integer.class)
    })
    public String podium_delete(HttpExchange t, JSONObject body) {
        try{
            PreparedStatement statement = connection.prepareStatement("DELETE FROM podia WHERE id = ?;");
            statement.setInt(1, body.getInt("id"));
            statement.execute();
            return "{\"success\":true}";
        }catch(Exception e) {
            e.printStackTrace();
            return "{\"success\":false}";
        }
    }

    //performances_get will get all performances in the database including their artist and podium.
    @RequestMethod("GET")
    public ResultSet performances_get(HttpExchange t) {
        try{
            Statement statement = connection.createStatement();
            return statement.executeQuery("SELECT pa.id,pa.`start`,pa.`end`,GROUP_CONCAT(JSON_OBJECT('id',a.id,'name',a.`name`,'description',a.description,'type',a.`type`))AS artist,GROUP_CONCAT(JSON_OBJECT('id',p.id,'name',p.`name`,'description',p.description,'location',p.`location`))AS podium FROM podia_artists pa INNER JOIN artists a ON a.id=pa.artist_id INNER JOIN podia p ON p.id=pa.podium_id GROUP BY pa.id;");
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //performances_create will create a new performance using the podium id, artist id, a start datetime and an end datetime.
    @RequestMethod("POST")
    @Required({
            @Value(name="podium", type=Integer.class),
            @Value(name="artist", type=Integer.class),
            @Value(name="start", type= String.class),
            @Value(name="end", type=String.class),
    })
    public String performances_create(HttpExchange t, JSONObject body) {
        try{
            PreparedStatement statement = connection.prepareStatement("INSERT INTO podia_artists(podium_id, artist_id, start, end) VALUES(?, ?, ?, ?);");
            statement.setInt(1, body.getInt("podium"));
            statement.setInt(2, body.getInt("artist"));
            DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            statement.setString(3, LocalDateTime.parse(body.getString("start").replace('T', ' '), format).toString());
            statement.setString(4, LocalDateTime.parse(body.getString("end").replace('T', ' '), format).toString());

            statement.execute();
            return "{\"success\":true}";
        }catch(Exception e) {
            e.printStackTrace();
            return "{\"success\":false}";
        }
    }

    //performance_get will return a single performance with its artist and podium using its id.
    @RequestMethod("POST")
    @Required({
            @Value(name="id", type=Integer.class)
    })
    public ResultSet performance_get(HttpExchange t, JSONObject body) {
        try{
            PreparedStatement statement = connection.prepareStatement("SELECT pa.id,pa.`start`,pa.`end`,GROUP_CONCAT(JSON_OBJECT('id',a.id,'name',a.`name`,'description',a.description,'type',a.`type`))AS artist,GROUP_CONCAT(JSON_OBJECT('id',p.id,'name',p.`name`,'description',p.description,'location',p.`location`))AS podium FROM podia_artists pa INNER JOIN artists a ON a.id=pa.artist_id INNER JOIN podia p ON p.id=pa.podium_id WHERE pa.id=?;");
            statement.setInt(1, body.getInt("id"));
            return statement.executeQuery();
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //performance_update will update a specific performance requiring its id, anything else will be checked and updated if included.
    @RequestMethod("POST")
    @Required({
            @Value(name="id", type=Integer.class)
    })
    public String performance_update(HttpExchange t, JSONObject body) {
        try{
            ArrayList<String> updated = new ArrayList<String>();
            ArrayList<Object> values = new ArrayList<Object>();
            DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            if(body.has("start")) {
                updated.add("start = ?");
                values.add(LocalDateTime.parse(body.getString("start").replace('T', ' '), format).toString());
            }
            if(body.has("end")) {
                updated.add("end = ?");
                values.add(LocalDateTime.parse(body.getString("end").replace('T', ' '), format).toString());
            }
            if(body.has("artist")) {
                updated.add("artist_id = ?");
                values.add(body.get("artist"));
            }
            if(body.has("podium")) {
                updated.add("podium_id = ?");
                values.add(body.get("podium"));
            }

            PreparedStatement statement = connection.prepareStatement("UPDATE podia_artists SET " + String.join(", ", updated) + " WHERE id = ?;");

            for(int i = 0; i < values.size(); i++) {
                statement.setObject(i+1, values.get(i));
            }
            statement.setInt(values.size()+1, body.getInt("id"));

            System.out.println(statement);

            statement.execute();
            return "{\"success\":true}";
        }catch(Exception e) {
            e.printStackTrace();
            return "{\"success\":false}";
        }
    }

    //performance_delete will delete a specific performance using its id.
    @RequestMethod("POST")
    @Required({
            @Value(name="id", type=Integer.class)
    })
    public String performance_delete(HttpExchange t, JSONObject body) {
        try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM podia_artists WHERE id = ?");
            statement.setInt(1, body.getInt("id"));
            statement.execute();
            return "{\"success\":true}";
        }catch(Exception e) {
            e.printStackTrace();
            return "{\"success\":false}";
        }
    }

}
