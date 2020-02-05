package nl.michaelmeir.avans.responders;

import io.github.cdimascio.dotenv.Dotenv;

import javax.xml.ws.spi.http.HttpExchange;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class API extends Responder {

    private Connection connection;

    public API(Dotenv env) {
        super("web", "/api", env, new HashMap<String, String>() {
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
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

//    public String get_bands(HttpExchange t) throws SQLException {
//        Statement statement = connection.createStatement();
//        statement.execute("SELECT * FROM bands");
//    }

}
