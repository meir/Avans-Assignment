package nl.michaelmeir.avans.main;

import nl.michaelmeir.avans.webserver.Webserver;

public class Main {

    //main is where the program gets called first, in here it will make a new web server and run it
    public static void main(String[] args) {
        Webserver server = new Webserver();
        server.run();
    }
}
