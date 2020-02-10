package nl.michaelmeir.avans.responders;

import com.sun.net.httpserver.HttpExchange;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Objects;

public class Pages extends Responder {

    public Pages(Dotenv env) {
        super("web", "/", env, new HashMap<String, String>());
    }

    @RequestMethod("GET")
    public byte[] index(HttpExchange t) throws IOException {
        String path = this.root + t.getRequestURI().getRawPath();
        if(path.endsWith("/")) {
            path += "index.html";
        }

        URL fileUrl = Objects.requireNonNull(getClass().getClassLoader().getResource(path));
        if(new File(fileUrl.getFile()).exists()) {
            StringBuilder content = new StringBuilder();
            try (FileReader reader = new FileReader(fileUrl.getFile());
                 BufferedReader br = new BufferedReader(reader)) {

                String line;
                while ((line = br.readLine()) != null) {
                    content.append(line);
                }
            }
            return content.toString().getBytes();
        }else{
            return NOT_FOUND.getBytes();
        }
    }

}
