package symbol;

public class TestCChannelThreads {
    public static void main(String[] args) throws Exception {
        CChannel canal = new CChannel("localhost", 5000);

        
        Thread produtor = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    System.out.println("Produtor envia: " + i);
                    canal.send(String.valueOf(i));
                    Thread.sleep(500); 
                }
                canal.send("fim"); 
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        
        Thread consumidor = new Thread(() -> {
            try {
                String msg;
                while (!(msg = canal.receive()).equals("fim")) {
                    int n = Integer.parseInt(msg);
                    long fat = 1;
                    for (int i = 2; i <= n; i++) fat *= i;
                    System.out.println("Consumidor recebe " + n + ", fatorial = " + fat);
                }
                System.out.println("Canal encerrado.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        produtor.start();
        consumidor.start();

        produtor.join();
        consumidor.join();
        canal.close();
    }
}
