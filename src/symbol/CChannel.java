package symbol;

import java.io.*;
import java.net.*;

public class CChannel {
    private Socket socket;
    private ServerSocket serverSocket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean isServer;

    public CChannel(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        isServer = true;
        System.out.println("[CChannel] Aguardando conexão na porta " + port);
        socket = serverSocket.accept(); 
        setupStreams();
        System.out.println("[CChannel] Cliente conectado!");
    }

    
    public CChannel(String host, int port) throws IOException {
        socket = new Socket(host, port);
        isServer = false;
        setupStreams();
        System.out.println("[CChannel] Conectado ao servidor " + host + ":" + port);
    }

    private void setupStreams() throws IOException {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true); // auto flush
    }

    public void send(String msg) {
        out.println(msg);
    }

    public String receive() throws IOException {
        return in.readLine();
    }

    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
        if (isServer && serverSocket != null) {
            serverSocket.close();
        }
        System.out.println("[CChannel] Canal fechado");
    }
}
