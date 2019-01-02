package bgu.spl.net.srv.bidi;

import bgu.spl.net.api.bidi.bidiMessageEncoderDecoder;
import bgu.spl.net.api.bidi.bidiMessagingProtocolImpl;
import bgu.spl.net.srv.Server;

public class bidiServerMain{

    public static void main(String[] args) {

        ServerDB _database = new ServerDB(); // maybe remove and bidiMessagingProtocolImpl gets new ?

// you can use any server...
        Server.threadPerClient(
                7777, //port
                () -> new bidiMessagingProtocolImpl(_database), //protocol factory
                () -> new bidiMessageEncoderDecoder() //message encoder decoder factory
        ).serve();

//        Server.reactor(
//                Runtime.getRuntime().availableProcessors(),
//                7777, //port
//                () ->  new bidiMessagingProtocolImpl(_database), //protocol factory
//                bidiMessageEncoderDecoder::new //message encoder decoder factory
//        ).serve();

    }

}