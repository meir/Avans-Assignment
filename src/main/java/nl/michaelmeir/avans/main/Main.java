package nl.michaelmeir.avans.main;

import nl.michaelmeir.avans.webserver.Webserver;

public class Main {

    /** 
     *
     *
     */
    public static void main(String[] args) {
        Webserver server = new Webserver();
        server.run();
    }
}
