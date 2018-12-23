package bgu.spl.net.impl.newsfeed;

import bgu.spl.net.api.bidi.bidiMessageEncoderDecoder;
import bgu.spl.net.api.bidi.bidiMessages;
import bgu.spl.net.api.bidi.bidiMessagingProtocolImpl;
import bgu.spl.net.srv.Server;

public class NewsFeedServerMain implements Runnable{

    public static void main(String[] args) {


    }

    @Override
    public void run() {
                NewsFeed feed = new NewsFeed(); //one shared object
//        String string = new String();
        bidiMessages.bidiMessage msg = new bidiMessages.bidiMessage("");


// you can use any server...
        Server.threadPerClient(
                7777, //port
                () -> new bidiMessagingProtocolImpl(), //protocol factory
                bidiMessageEncoderDecoder::new //message encoder decoder factory
        ).serve();

//        Server.reactor(
//                Runtime.getRuntime().availableProcessors(),
//                7777, //port
//                () ->  new bidiMessagingProtocolImpl(), //protocol factory
//                bidiMessageEncoderDecoder::new //message encoder decoder factory
//        ).serve();
    }
}
