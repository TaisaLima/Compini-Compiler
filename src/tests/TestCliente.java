import symbol.CChannel;

public class TestCliente {
    public static void main(String[] args) throws Exception {
        CChannel client = new CChannel("localhost", 5000); // conecta ao servidor
        client.send("Mensagem de teste");
        String resp = client.receive();
        System.out.println("[Cliente] Recebido: " + resp);
        client.close();
    }
}
