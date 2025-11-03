package main.semantic.types;

import java.io.*;
import java.net.*;
import main.lexer.*;

// Extende Type, conforme o padrão do compilador
public class ChannelType extends Type {

    private String host;
    private int port;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    // ✅ Construtor padrão (usado na tabela de símbolos)
    public ChannelType() {
        // Ajuste os valores de tag e width conforme sua convenção
        super("cchannel", Tag.CCHANNEL, 0);
    }

    // ✅ Construtor de runtime (usado em VarCChannel)
    public ChannelType(String host, int port) {
        super("cchannel", Tag.CCHANNEL, 0);
        this.host = host;
        this.port = port;
    }

    public void connect() {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("[ChannelType] Conectado a " + host + ":" + port);
        } catch (IOException e) {
            System.err.println("[ChannelType] Erro ao conectar: " + e.getMessage());
        }
    }

    public void send(String msg) {
        if (out != null) {
            out.println(msg);
            System.out.println("[ChannelType] Enviado: " + msg);
        } else {
            System.err.println("[ChannelType] Canal não conectado para envio.");
        }
    }

    public String receive() {
        try {
            if (in != null) {
                String msg = in.readLine();
                System.out.println("[ChannelType] Recebido: " + msg);
                return msg;
            }
        } catch (IOException e) {
            System.err.println("[ChannelType] Erro ao receber: " + e.getMessage());
        }
        return null;
    }

    public void close() {
        try {
            if (socket != null) socket.close();
            System.out.println("[ChannelType] Canal fechado.");
        } catch (IOException e) {
            System.err.println("[ChannelType] Erro ao fechar canal: " + e.getMessage());
        }
    }
}
