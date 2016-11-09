/*
    Gestione Soci Protezione Civile
    Copyright (C) Simeosoft di Carlo Simeone
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package proci;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author simeo
 */
public class WebHandler implements HttpHandler {
    static final Logger logger = LoggerFactory.getLogger(WebHandler.class);     
    
    @Override
    public void handle(HttpExchange t) throws IOException {
        logger.debug("HttpExchange from: {}",t.getRemoteAddress().getHostString());
        for (Map.Entry entry  : t.getRequestHeaders().entrySet()) {
            logger.debug("header: {},{}",entry.getKey(),entry.getValue());
        }
        URI uri = t.getRequestURI();
        logger.debug("Path: {}",uri.getPath());
        logger.debug("Query: {}",uri.getQuery());
        String page =  uri.getPath().replaceAll(App.HTTPCONTEXT, "").substring(1);
        logger.debug("Page: {}",page);
        //
        int code = 0;
        String response;
        JSONObject respJSON = new JSONObject();
        t.getResponseHeaders().add("Content-type:", "application/json");
        // tratto per semplicità allo stesso modo tutti i request method (POST,GET..)
        switch (page) {
            // funzione di status
            case "status":
                respJSON.put("status", "OK");
                respJSON.put("message", "Proci ver.: " + App.VERSION);
                code = 200;
                break;
            // upload immagine (solo se nella funzione)
            case "send":
                StringWriter sw = new StringWriter();
                IOUtils.copy(t.getRequestBody(), sw, "UTF-8");
                String in = sw.toString();
                logger.debug("String in: {}",in);
                JSONParser parser = new JSONParser();
                try {
                    JSONObject obj2 = (JSONObject) parser.parse(in);
                } catch (ParseException pe) {
                    respJSON.put("status", "ERRORE!");
                    respJSON.put("message", "PARSE EXCEPTION: " + pe.getLocalizedMessage());
                }
                code = 200;
                break;
            default:
                respJSON.put("status", "ERRORE!");
                respJSON.put("message", "Pagina non trovata!");
                code = 404;
        }
        // risposta
        response = respJSON.toJSONString();        
        t.sendResponseHeaders(code, response.getBytes("UTF-8").length);
        try (OutputStream os = t.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
