package bgu.spl.net.srv.bidi;

import bgu.spl.net.api.bidi.bidiMessageEncoderDecoder;
import bgu.spl.net.api.bidi.bidiMessages;
import bgu.spl.net.api.bidi.bidiMessagingProtocolImpl;
import bgu.spl.net.srv.Server;
import bgu.spl.net.srv.bidi.ServerDB;

public class bidiServerMain {

    public static void main(String[] args) {

        ServerDB _database = new ServerDB();

// you can use any server...
        Server.threadPerClient(
                7777, //port
                () -> new bidiMessagingProtocolImpl(_database), //protocol factory
                bidiMessageEncoderDecoder::new //message encoder decoder factory
        ).serve();

//        Server.reactor(
//                Runtime.getRuntime().availableProcessors(),
//                7777, //port
//                () ->  new bidiMessagingProtocolImpl(_database), //protocol factory
//                bidiMessageEncoderDecoder::new //message encoder decoder factory
//        ).serve();

    }
}