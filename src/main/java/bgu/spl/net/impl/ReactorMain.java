package bgu.spl.net.impl;

import bgu.spl.net.api.bidi.bidiMessageEncoderDecoder;
import bgu.spl.net.api.bidi.bidiMessagingProtocolImpl;
import bgu.spl.net.srv.Server;
import bgu.spl.net.srv.bidi.ServerDB;

public class ReactorMain{

    public static void main(String[] args) {

        ServerDB _database = new ServerDB();

        Server.reactor(
                Runtime.getRuntime().availableProcessors(),
                7777, //port
                () ->  new bidiMessagingProtocolImpl(_database), //protocol factory
                bidiMessageEncoderDecoder::new //message encoder decoder factory
        ).serve();

    }

}