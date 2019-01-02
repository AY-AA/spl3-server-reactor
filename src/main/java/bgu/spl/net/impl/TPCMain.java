package bgu.spl.net.impl;

import bgu.spl.net.api.bidi.bidiMessageEncoderDecoder;
import bgu.spl.net.api.bidi.bidiMessagingProtocolImpl;
import bgu.spl.net.srv.Server;
import bgu.spl.net.srv.bidi.ServerDB;

public class TPCMain {

    public static void main(String[] args) {

        ServerDB _database = new ServerDB();
        int port =  Integer.parseInt(args[0]);

        Server.threadPerClient(
                port, //port
                () -> new bidiMessagingProtocolImpl(_database), //protocol factory
                () -> new bidiMessageEncoderDecoder() //message encoder decoder factory
        ).serve();

    }



}

