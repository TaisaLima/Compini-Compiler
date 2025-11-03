package main.ast.stmt;

import main.ast.core.Node;

public class ChannelDeclNode implements Node {

    private final String channelName;
    private final String clientId;
    private final String serverId;

 

    public ChannelDeclNode(String channelName, String clientId, String serverId) {
        this.channelName = channelName;
        this.clientId = clientId;
        this.serverId = serverId;
    }

  

    public String getChannelName() {
        return channelName;
    }

    public String getClientId() {
        return clientId;
    }

    public String getServerId() {
        return serverId;
    }


    @Override
    public String toString() {
        return "CHANNEL_DECL [Name: " + channelName + ", Client: " + clientId + ", Server: " + serverId + "]";
    }
}