package bgu.spl.net.impl.stomp;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import bgu.spl.net.impl.Sub;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

public class ConnectionsImpl<T> implements Connections<T>{
    private Map<Integer, ConnectionHandler<T>> clients = new ConcurrentHashMap<>();
    private Map<String, Sub> subs = new ConcurrentHashMap<>();
    private Map<String, List<String>> channels = new ConcurrentHashMap<>();


    @Override
    public boolean send(int connectionId, T msg) {
        ConnectionHandler<T> client = clients.get(connectionId);
        if (client != null) {
            System.out.println("Sending message to client " + connectionId + ": " + msg);
            client.send(msg);
            return true;
        }
        return false;
        
    }

    @Override
    public void send(String channel, T msg) {
        if (channels.containsKey(channel)) {
            for (String con_subID : channels.get(channel)) {
                send(getConnectionId(con_subID), msg);
            }
        }
    }

    @Override
    public void disconnect(int connectionId) {
        for (Map.Entry<String, Sub> entry : subs.entrySet()) {
            Sub sub = entry.getValue();
            if (sub.getId() == connectionId) {
                String channel = sub.getName();
                if (channels.containsKey(channel)) {
                    channels.get(channel).remove(entry.getKey());
                }
                subs.remove(entry.getKey());
            }
        }
        clients.remove(connectionId);
    }

    public void addClient(int connectionId, ConnectionHandler<T> client) {
        clients.put(connectionId, client);
    }   
    
    public void unsubscribe(String con_subID){
        if (subs.containsKey(con_subID)) {
            channels.get(subs.get(con_subID).getName()).remove(con_subID);
            subs.remove(con_subID);
            return;
        }
        throw new IllegalArgumentException("no such subID");
    }

    public void subscribe(String destination, int connectionID, String id){
        for (Map.Entry<String, Sub> entry : subs.entrySet()) {
            if (entry.getKey().equals(Integer.toString(connectionID)+"+"+id)) {
                throw new IllegalArgumentException("sub already exists");
            }
        }
        Sub newSub = new Sub(connectionID, destination);
        subs.put(Integer.toString(connectionID)+"+"+id, newSub);//yagel maybe error
        channels.putIfAbsent(destination, new java.util.concurrent.CopyOnWriteArrayList<>());
        channels.get(destination).add(Integer.toString(connectionID)+"+"+id);
    }

    public int getConnectionId(String ID){
        Sub sub = subs.get(ID);
        if(sub != null){
            return sub.getId();
        }
        throw new IllegalArgumentException("no such subID");
    }

    public List<String> getSubscribers(String channel) {
        return channels.get(channel);
    }
}
