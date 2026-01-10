package bgu.spl.net.impl.stomp;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import bgu.spl.net.impl.Sub;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

public class ConnectionsImpl<T> implements Connections<T>{
    private Map<Integer, ConnectionHandler<T>> clients = new ConcurrentHashMap<>();
    private Map<Integer, Sub> subs = new ConcurrentHashMap<>();
    private String[] channels;//yagel
    private int subIDCounter;//equal to the last subID that exists in the db


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
        int i=0;
        for(; i<channels.length; i++){
            if (channels[i].equals(channel)) {
                break;
            }
        }
        if (i==channels.length) {
            //not exist
        }
        for(Sub s : subs.values()){
            if (s.getName().equals(channel)) {
                send(s.getId(), msg);
            }
        }
        //maybe change to channels map yagel
    }

    @Override
    public void disconnect(int connectionId) {
        if(send(connectionId, null)){//build disconnect message
            // recipt yagel
            try{
                clients.get(connectionId).close();
                clients.remove(connectionId);
            }catch(IOException e){
                //yagel
            }
        }
        else{
            throw new IllegalArgumentException("no such client");
        }
    }

    //add new client yagel
    public void addClient(int connectionId, ConnectionHandler<T> client) {
        clients.put(connectionId, client);
    }   
    
    public void unsubscribe(int subID){
        if (subs.containsKey(subID)) {
            subs.remove(subID);
            return;
        }
        throw new IllegalArgumentException("no such subID");
    }

    public void subscribe(String destination, int connectionID){
        Sub newSub = new Sub(connectionID, destination);
        subs.put(subIDCounter, newSub);
        subIDCounter++;
    }
}
