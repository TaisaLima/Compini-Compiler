package main.runtime.channel;

import java.io.IOException;

public class ChannelRuntime {
    
    public ChannelRuntime(String host, int port) {
    }

    public void send(String msg) {
    }

    public String receive() throws IOException {
        return "dados_recebidos"; 
    }

    public void close() throws IOException {
    }
}