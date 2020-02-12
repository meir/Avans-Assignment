package nl.michaelmeir.avans.responders;

import com.sun.net.httpserver.HttpExchange;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Objects;

//Pages handles all default calls without an /api/ prefix
public class Pages extends Responder {

    //Pages initializes the Responder
    public Pages(Dotenv env) {
        super(env.get("ROOT", "web"), "/", env, new HashMap<String, String>());
    }

    //index returns files if they can be found that correspond to the url
    @RequestMethod("GET")
    public byte[] index(HttpExchange t) throws IOException {
        String path = this.root + t.getRequestURI().getRawPath();
        if(path.endsWith("/")) {
            path += "index.html";
        }

        URL fileUrl = Objects.requireNonNull(getClass().getClassLoader().getResource(path));
        if(new File(fileUrl.getFile()).exists()) {
            String type = URLConnection.guessContentTypeFromName(path);
            if(type != null && type.length() > 0) {
                t.getResponseHeaders().add("Content-Type", type);
            }

            InputStream stream = fileUrl.openStream();
            return IOUtils.toByteArray(stream);
        }else{
            return NOT_FOUND.getBytes();
        }
    }

}
