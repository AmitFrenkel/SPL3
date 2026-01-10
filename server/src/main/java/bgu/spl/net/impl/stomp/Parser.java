package bgu.spl.net.impl.stomp;

import java.util.concurrent.atomic.AtomicInteger;

public class Parser {
    private static AtomicInteger messageID=new AtomicInteger(0);
    public static String buildConnectedFrame(){
        return "CONNECTED\nversion:1.2\n\n";
    }

    public static String buildReciptFrame(String id){
        return "RECEIPT\nreceipt-id:" + id + "\n\n";
    }

    public static String buildMessageFrame(String subscription, String destination, 
        String body){
            return "MESSAGE\nsubscription:" + subscription + "\nmessage-id:" + messageID.getAndIncrement() +
            "\ndestination:" + destination + "\n\n" + body+"\n";
    }

    public static String buildErrorFrame(String receiptId, String frame, String error){
        return "ERROR\nreceipt-id:" + receiptId + "\nmessage: malformed frame received" + 
        "The message:\n-----\n" + frame + "\n-----\n" + error + "\n";
    }
    public static String buildErrorFrame(String frame, String error){
        return "ERROR\nmessage: malformed frame received" + 
        "The message:\n-----\n" + frame + "\n-----\n" + error + "\n";
    }
}
