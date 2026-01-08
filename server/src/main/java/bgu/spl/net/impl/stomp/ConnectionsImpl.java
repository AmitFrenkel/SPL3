package bgu.spl.net.impl.stomp;
import java.util.List;
import java.util.Map;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

public class ConnectionsImpl<T> implements Connections<T>{
    private Map<Integer, ConnectionHandler<T>> clients;
    private Map<String, List<Integer>> channels;

    public ConnectionsImpl(Map<Integer, ConnectionHandler<T>> clients) {
        this.clients = clients;
    }

    @Override
    public boolean send(int connectionId, T msg) {
        ConnectionHandler<T> client = clients.get(connectionId);
        if (client != null) {
            client.send(msg);
            return true;
        }
        return false;
        
    }

    @Override
    public void send(String channel, T msg) {
        for(Integer id : channels.get(channel)) {
            send(id, msg);
        }
    }

    @Override
    public void disconnect(int connectionId) {
        if(send(connectionId, null))// recipt yagel
            clients.remove(connectionId);
        else{
            throw new IllegalArgumentException("no such client");
        }
    }

    //add new client yagel
    
}
