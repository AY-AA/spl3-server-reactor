package bgu.spl.net.srv;

import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.bidiMessageEncoderDecoder;
import bgu.spl.net.api.bidi.bidiMessages;
import bgu.spl.net.api.bidi.bidiMessagingProtocolImpl;
import bgu.spl.net.impl.newsfeed.NewsFeedClientMain;
import bgu.spl.net.srv.bidi.ServerDB;
import bgu.spl.net.srv.bidi.bidiServerMain;

import javax.print.DocFlavor;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class TMP implements Runnable{
    public static void main(String []args) throws Exception {
        test1();
    }

    private static void test1() throws IOException {
        Thread t = new Thread(new TMP());
        t.start();

        try {
            Thread.currentThread().sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Client1 c1 = new Client1("127.0.0.1", 7777);
        Thread t1 = new Thread(c1);
        t1.start();

        Client2 c2 = new Client2("127.0.0.1", 7777);
        Thread t2 = new Thread(c2);
        t2.start();

        Client3 c3 = new Client3("127.0.0.1", 7777);
        Thread t3 = new Thread(c3);
        t3.start();
    }

    @Override
    public void run() {
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
