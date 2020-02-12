package nl.michaelmeir.avans.webserver;

import com.sun.net.httpserver.HttpServer;
import io.github.cdimascio.dotenv.Dotenv;
import nl.michaelmeir.avans.responders.API;
import nl.michaelmeir.avans.responders.Pages;

import java.net.InetSocketAddress;

//Webserver initializes the webserver and handlers
public class Webserver {

    public int PORT = 8080; //PORT is the default port that will be used if its not specified in the .env
    public static Dotenv env; //env contains the data of the .env file

    private HttpServer server; //server is the webserver

    //Webserver initializes the webserver with its port
    public Webserver() {
        try{
            env = Dotenv.load();
            PORT = Integer.parseInt(env.get("PORT", "8080"));
            System.out.println(PORT);
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
        }catch(Exception e) {
            System.out.println("Could not start webserver on port 80.");
            e.printStackTrace();
            System.exit(1);
        }
    }

    //run starts the webserver with its Page and API responders
    public void run() {
        System.out.println("Starting webserver on http://localhost:" + String.valueOf(PORT));
        server.createContext("/", new Pages(env));
        server.createContext("/api/", new API(env));
        server.start();
    }

}
