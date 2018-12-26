package bgu.spl.net.impl.newsfeed;

import bgu.spl.net.api.bidi.bidiMessages;
import bgu.spl.net.impl.rci.RCIClient;

public class NewsFeedClientMain {

    public static void main(String[] args) throws Exception {
        if (args == null || args.length == 0) {
            args = new String[]{"127.0.0.1"};
        }

        System.out.println("running clients");
        runSecondClient(args[0]);

        runFirstClient(args[0]);
//        runSecondClient(args[0]);
//        runThirdClient(args[0]);
    }

    private static void runFirstClient(String host) throws Exception {
        try (RCIClient c = new RCIClient(host, 7777)) {
            c.send(new bidiMessages.bidiMessage("REGISTER alex 111"));
            System.out.println("REGISTER alex");

            c.send(new bidiMessages.bidiMessage("LOGIN alex 111"));
            System.out.println("LOGIN avishai");

            c.send(new bidiMessages.bidiMessage(
                    "POST hey @avishai how are u"));
            System.out.println("msg1 sent");




//            c.receive(); //ok
//
//            c.send(new bidiMessages.bidiMessage(
//                    "POST new SPL assignment is out soon!!"));
//            System.out.println("msg2 sent");
//
//            c.receive(); //ok
//
//            c.send(new bidiMessages.bidiMessage(
//                    "POST THE CAKE IS A LIE!"));
//            System.out.println("msg3 sent");
//
//            c.receive(); //ok
        }

    }

    private static void runSecondClient(String host) throws Exception {
        try (RCIClient c = new RCIClient(host, 7777)) {
            c.send(new bidiMessages.bidiMessage("REGISTER avishai 1111"));
            System.out.println("REGISTER avishai");

            c.send(new bidiMessages.bidiMessage("LOGIN avishai 1111"));
            System.out.println("LOGIN avishai");

            Thread t = new Thread(c);
            t.start();

//            c.send(new bidiMessages.bidiMessage("jobs"));
//            System.out.println("second client received: " + c.receive());
        }
    }

    private static void runThirdClient(String host) throws Exception {
        try (RCIClient c = new RCIClient(host, 7777)) {
            c.send(new bidiMessages.bidiMessage("headlines"));
            System.out.println("third client received: " + c.receive());
        }
    }
}
