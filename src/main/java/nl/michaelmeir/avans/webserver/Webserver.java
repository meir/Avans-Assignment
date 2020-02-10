package nl.michaelmeir.avans.webserver;

import com.sun.net.httpserver.HttpServer;
import io.github.cdimascio.dotenv.Dotenv;
import nl.michaelmeir.avans.responders.API;
import nl.michaelmeir.avans.responders.Pages;

import java.net.InetSocketAddress;

public class Webserver {

    public int PORT = 8080;
    public static String INDEX = "index.html";
    public static String ROOT = "/web";
    public static Dotenv env;

    private HttpServer server;

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

    public void run() {
        System.out.println("Starting webserver on http://localhost:" + String.valueOf(PORT));
        server.createContext("/", new Pages(env));
        server.createContext("/api/", new API(env));
        server.start();
    }

}
