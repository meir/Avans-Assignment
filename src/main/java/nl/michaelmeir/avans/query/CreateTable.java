package nl.michaelmeir.avans.query;

import java.sql.Connection;
import java.util.HashMap;

public class CreateTable {
    public CreateTable(Connection connection, String table, HashMap<String, String> fields) {
        try {
            String values = "`id` int(11) NOT NULL auto_increment,";
            for(String key : fields.keySet()) {
                values += "`" + key + "` " + fields.get(key) + ","; 
            }
            values += "PRIMARY KEY  (`id`)";
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS " + table + " ( " + values + " );");
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
}