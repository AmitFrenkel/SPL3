package bgu.spl.net.impl.stomp;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.impl.data.Database;
import bgu.spl.net.impl.data.LoginStatus;
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
        System.out.println("Processing message: " + message);
        String[] parts = message.split("\n\n");
        String[] headersHolders = buildFormat(parts[0]);
        headers.clear();
        body = null;
        for (String line : headersHolders) {
            String[] keyValue = line.split(":");
            headers.put(keyValue[0], keyValue[1]);
        }
        body = parts.length > 1 ? parts[1] : null;
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
            if (headers.get("receipt") != null && command != CommandEnum.CONNECT) {
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
        }
        return Arrays.copyOfRange(lines, 1, lines.length);
    }
    private void connect(){
        if (!headers.containsKey("host")) {
            throw new IllegalArgumentException("no host");
        }
        if (!headers.containsKey("login")) {
            throw new IllegalArgumentException("no login");
        }
        if (!headers.containsKey("passcode")) {
            throw new IllegalArgumentException("no passcode");
        }
        LoginStatus lg = Database.getInstance().login(connectionID, headers.get("login"), headers.get("passcode"));
        if (lg == LoginStatus.ALREADY_LOGGED_IN) {
            throw new IllegalArgumentException("User already logged in");
        }
        else if (lg == LoginStatus.WRONG_PASSWORD) {
            throw new IllegalArgumentException("Wrong password");
        }
        connections.send(connectionID, Parser.buildConnectedFrame());
    }
    private void subscribe(){
        if(!headers.containsKey("destination")){
            throw new IllegalArgumentException("no destination");
        }
        if (!headers.containsKey("id")) {
            throw new IllegalArgumentException("no id");
        }
        //create new sub
        ((ConnectionsImpl)connections).subscribe(headers.get("destination"), connectionID, headers.get("id"));
    }
    private void unsubscribe(){
        if (!headers.containsKey("id")) {
            throw new IllegalArgumentException("no id");
        }
        ((ConnectionsImpl)connections).unsubscribe(connectionID+"+"+headers.get("id"));
    }



    private void send(){
        if(body == null){
            throw new IllegalArgumentException("no body");
        }
        if(!headers.containsKey("destination")){
            throw new IllegalArgumentException("no destination");
        }
        if (!headers.containsKey("fileName")) {
            throw new IllegalArgumentException("no fileName");
        }
        List<String> getSubscribers = ((ConnectionsImpl)connections).getSubscribers(headers.get("destination"));
        if(getSubscribers == null){
            throw new IllegalArgumentException("no such destination");
        }
        else{
            Database.getInstance().trackFileUpload(body.split("\n")[0].split(":")[1], headers.get("fileName"), headers.get("destination"));
            boolean isSubscribed = false;
            for (String con_subID : getSubscribers) {
                int connID = ((ConnectionsImpl)connections).getConnectionId(con_subID);
                if (connID == connectionID) {
                    isSubscribed = true;
                    break;
                }
            }
            if (!isSubscribed) {
                throw new IllegalArgumentException("user not subscribed to destination");
            }
            for(String con_subID : getSubscribers){
                int connectionID = ((ConnectionsImpl)connections).getConnectionId(con_subID);
                
                String fullMsg = Parser.buildMessageFrame(con_subID.split("\\+")[1], headers.get("destination"), body);
                connections.send(connectionID, fullMsg);
            }
        }        
    }

    
    private void disconnectUserRequest(){
        if (!headers.containsKey("receipt")) {
            throw new IllegalArgumentException("no receipt");
        }
        disconnect();
    }
    private void disconnect(){
        shouldTerminate = true;
        connections.send(connectionID, Parser.buildReciptFrame(headers.get("receipt")));
        Database.getInstance().logout(connectionID);
        connections.disconnect(connectionID);
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
        disconnect();
    }

    public void report(){
        Database.getInstance().printReport();
    }

}
