package bgu.spl.net.impl.stomp;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.Connections;

public class StompMessagingProtocolImpl implements StompMessagingProtocol<String>{
    private CommandEnum command;
    private boolean shouldTerminate = false;
    private Map<String, String> headers = new ConcurrentHashMap<>();
    private String body;
    private int connectionID;
    private Connections<String> connections;


    @Override
    public void start(int connectionId, Connections<String> connections) {
        this.connectionID = connectionId;
        this.connections = connections;
    }

    @Override
    public void process(String message) {
        String[] lines = buildFormat(message);
        headers.clear();
        body = null;
        int i = 0;
        while(!lines[i].equals("")){
            String[] header = lines[i].split(":");
            headers.put(header[0], header[1]);
            i++;
        }
        i++;
        StringBuilder temp = new StringBuilder();
        while(lines[i] != "\0"){
            temp.append(lines[i]);
        }
        body = temp.toString();
        try{
            switch (command) {
                case CONNECT:
                    connect();
                    break;
                case SUBSCRIBE:
                    subscribe();
                    break;
                case UNSUBSCRIBE:
                    unsubscribe();
                    break;
                case SEND:
                    send();
                    break;
                case DISCONNECT:
                    disconnectUserRequest();
            }
            if (headers.get("receipt") != null && command != CommandEnum.DISCONNECT) {
                recipt(headers.get("receipt"));
            }
        }catch(Exception e){
            error(e.getMessage(), message);
        }

    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    private String[] buildFormat(String message){
        String[] lines = message.toString().split("\n");
        String command = lines[0];
        switch (command) {
            case "CONNECT":
                this.command = CommandEnum.CONNECT;
                break;
            case "SEND":
                this.command = CommandEnum.SEND;
                break;
            case "SUBSCRIBE":
                this.command = CommandEnum.SUBSCRIBE;
                break;
            case "UNSUBSCRIBE":
                this.command = CommandEnum.UNSUBSCRIBE;
                break;
            case "DISCONNECT":
                this.command = CommandEnum.DISCONNECT;
                this.shouldTerminate = true;
                break;
            case "MESSAGE":
                this.command = CommandEnum.MESSAGE;
                break;
            // case "ERROR":
            //     this.command = CommandEnum.ERROR;
            //     break;
        }
        return Arrays.copyOfRange(lines, 1, lines.length);
    }
    private void connect(){
        // if (!headers.containsKey("accept-version")) {
        //     throw new IllegalArgumentException("no accept version");
        // }//ygael
        // if (headers.get("accept-version") != "1.2") {
        //     throw new IllegalArgumentException("wrong version");
            
        // }
        if (!headers.containsKey("host")) {
            throw new IllegalArgumentException("no host");
        }
        if (!headers.containsKey("login")) {
            throw new IllegalArgumentException("no login");
        }
        if (!headers.containsKey("passcode")) {
            throw new IllegalArgumentException("no passcode");
        }
        // add checks for connection to host
        // checks if the user exist and the passcode is correct
        // create connection
        //connections.send(this.connectionID, msg)
        connections.send(connectionID, Parser.buildConnectedFrame());
    }
    private void subscribe(){
        if(!headers.containsKey("destination")){
            throw new IllegalArgumentException("no destination");
        }
        //checks if dest exist
        if (!headers.containsKey("id")) {
            throw new IllegalArgumentException("no id");
        }
        //create new sub
        ((ConnectionsImpl)connections).subscribe(headers.get("destination"), connectionID);
    }
    private void unsubscribe(){
        if (!headers.containsKey("id")) {
            throw new IllegalArgumentException("no id");
        }
        ((ConnectionsImpl)connections).unsubscribe(Integer.parseInt(headers.get("id")));
    }
    private void send(){
        if(body == null){
            throw new IllegalArgumentException("no body");
        }
        connections.send(headers.get("destination"), body);//build the foramt from SEND to MESSAGE
        
    }
    private void disconnectUserRequest(){
        if (!headers.containsKey("receipt")) {
            throw new IllegalArgumentException("no receipt");
        }
        recipt(headers.get("receipt"));
        disconnect();
    }
    private void disconnect(){
        shouldTerminate = true;
        connections.disconnect(connectionID);
        connections.send(connectionID, Parser.buildReciptFrame(headers.get("receipt")));
    }
    private void recipt(String receiptID){
        connections.send(connectionID, Parser.buildReciptFrame(receiptID));
    }

    private void error(String errorMessage, String message){
        if (headers.get("receipt") != null) {
            connections.send(connectionID, Parser.buildErrorFrame(headers.get("receipt"), errorMessage, message));
        } else {
            connections.send(connectionID, Parser.buildErrorFrame(message, errorMessage));
            
        }
    }

}
