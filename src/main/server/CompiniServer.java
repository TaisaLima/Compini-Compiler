package main.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import main.Main; 

public class CompiniServer {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        System.out.println("Servidor Compini rodando em http://localhost:8080");

        server.createContext("/compilar", new CompilarHandler());
        server.setExecutor(null);
        server.start();
    }

    static class CompilarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, -1); // Método não permitido
                return;
            }

            InputStream is = exchange.getRequestBody();
            String code = new BufferedReader(new InputStreamReader(is))
                    .lines().reduce("", (acc, line) -> acc + line + "\n");

            String response;
            try {
                // Chama seu compilador Compini para compilar a expressão
                Main mainCompini = new Main();
                response = mainCompini.compilarExpressaoParaWeb(code); 
                // Método que você precisa criar para retornar JSON ou string
            } catch (Exception e) {
                response = "{ \"erro\": \"" + e.getMessage() + "\" }";
            }

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
