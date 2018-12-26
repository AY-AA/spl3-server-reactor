package bgu.spl.net.impl.newsfeed;

import bgu.spl.net.api.bidi.bidiMessages;
import bgu.spl.net.impl.rci.RCIClient;
import bgu.spl.net.srv.Client1;
import bgu.spl.net.srv.Client2;

public class NewsFeedClientMain {

    public static void main(String[] args) throws Exception {
        if (args == null || args.length == 0) {
            args = new String[]{"127.0.0.1"};
        }

        System.out.println("running clients");

    }
}
