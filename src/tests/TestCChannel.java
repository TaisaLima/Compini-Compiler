package tests;

import symbol.*;

public class TestCChannel {
    public static void main(String[] args) throws Exception {
        int port = 12345;

        // servidor
        new Thread(() -> {
            try {
                CChannel server = new CChannel("localhost",port); // server mode
                String msg = server.receive();
                System.out.println("[Servidor] Recebido: " + msg);
                server.send("OK: " + msg);
                server.close();
            } catch(Exception e){ e.printStackTrace(); }
        }).start();

        // cliente
        Thread.sleep(500); 
        CChannel client = new CChannel("localhost", port);
        client.send("TesteCChannel");
        String resposta = client.receive();
        System.out.println("[Cliente] Recebido: " + resposta);
        client.close();
    }
}
