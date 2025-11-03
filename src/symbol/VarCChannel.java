package symbol;

import java.io.IOException;

public class VarCChannel extends Var {

    private CChannel channel;


    public VarCChannel(String nome) {
        super(nome, Type.CCHANNEL); 

    }
    public void connect(String host, int port) throws IOException {
        channel = new CChannel(host, port);
    }

    public void send(String msg) {
        if(channel != null) {
            channel.send(msg);
        } else {
            throw new IllegalStateException("Canal não inicializado");
        }
    }

    public String receive() throws IOException {
        if(channel != null) {
            return channel.receive();
        } else {
            throw new IllegalStateException("Canal não inicializado");
        }
    }

    public void close() throws IOException {
        if(channel != null) {
            channel.close();
        }
    }

    public CChannel getChannel() {
        return channel;
    }
}
