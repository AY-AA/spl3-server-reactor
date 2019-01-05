package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.bidi.bidiMessageEncoderDecoder;
import bgu.spl.net.api.bidi.bidiMessagingProtocolImpl;
import bgu.spl.net.srv.Server;
import bgu.spl.net.srv.bidi.ServerDB;

public class ReactorMain{

    public static void main(String[] args) {

        ServerDB _database = new ServerDB();
        int port = 0;
        int numOfThreads = 0;

        if (args.length > 1) {
            port = Integer.parseInt(args[0]);
            numOfThreads = Integer.parseInt(args[1]);
        }

//        int port =  7777;
//        int numOfThreads = 10;

        Server.reactor(
                numOfThreads,
                port, //port
                () ->  new bidiMessagingProtocolImpl(_database), //protocol factory
                bidiMessageEncoderDecoder::new //message encoder decoder factory
        ).serve();

    }

}