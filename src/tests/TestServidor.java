import symbol.CChannel;

public class TestServidor {
    public static void main(String[] args) throws Exception {
        CChannel server = new CChannel(5000); 
        String msg = server.receive();
        System.out.println("[Servidor] Recebido: " + msg);
        server.send("OK: " + msg);
        server.close();
    }
}
