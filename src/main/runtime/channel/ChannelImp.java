package main.runtime.channel;

import main.semantic.types.*;

public class ChannelImp extends Var {

    private ChannelType channel;

    public ChannelImp(String nome, String host, int port) {
        super(nome, new ChannelType());      // ✅ substitui CChannel()
        this.channel = new ChannelType(host, port); // ✅ idem
    }

    // Métodos utilitários
    public void connect() {
        channel.connect();
    }

    public void send(String msg) {
        channel.send(msg);
    }

    public String receive() {
        return channel.receive();
    }

    public void close() {
        channel.close();
    }

    public CChannel getChannel() {
        return channel;
    }
}
