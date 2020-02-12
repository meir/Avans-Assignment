package nl.michaelmeir.avans.query;

import java.sql.Connection;
import java.util.HashMap;

//CreateTable is a class to quickly and easily create a table without using too much mysql
public class CreateTable {

    //CreateTable creates a table in the database using the connection to it, a name and an hashmap containing all the fields
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

    //CreateTable is another form of the previous CreateTable but in here you can add additional SQL lines for many to many relations or other things.
    public CreateTable(Connection connection, String table, HashMap<String, String> fields, String... additional) {
        try {
            String values = "`id` int(11) NOT NULL auto_increment,";
            for(String key : fields.keySet()) {
                values += "`" + key + "` " + fields.get(key) + ","; 
            }
            values += "PRIMARY KEY  (`id`), ";
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS " + table + " ( " + values + String.join(", ", additional) + " );");
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
}