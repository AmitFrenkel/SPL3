package bgu.spl.net.impl.stomp;

import bgu.spl.net.impl.echo.LineMessageEncoderDecoder;
import bgu.spl.net.srv.Server;

public class StompServer {

    public static void main(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("You must provide server type and port number");
        }
        int port;
        try{
            port = Integer.parseInt(args[0]);
        }catch (NumberFormatException e){
            throw new IllegalArgumentException("First argument must be an integer representing the port number");
        }
        if (args[1].equals("tpc")) {
            Server.threadPerClient(
                    port, //port
                    () -> new StompMessagingProtocolImpl(), //protocol factory
                    StompMessageEncoderDecoder::new //message encoder decoder factory
            ).serve();
        }
        else if (args[1].equals("reactor")){
            Server.reactor(
                    Runtime.getRuntime().availableProcessors(),
                    port, //port
                    () -> new StompMessagingProtocolImpl(), //protocol factory
                    StompMessageEncoderDecoder::new //message encoder decoder factory
            ).serve();
        }else{
            throw new IllegalArgumentException("Unknown server type " + args[1]);
        }
    }
}
