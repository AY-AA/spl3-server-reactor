package bgu.spl.net.impl.newsfeed;

import bgu.spl.net.impl.rci.RCIClient;

public class NewsFeedClientMain {

    public static void main(String[] args) throws Exception {
        if (args == null || args.length == 0) {
            args = new String[]{"127.0.0.1"};
        }

        System.out.println("running clients");
        runFirstClient(args[0]);
        runSecondClient(args[0]);
        runThirdClient(args[0]);
    }

    private static void runFirstClient(String host) throws Exception {
        try (RCIClient c = new RCIClient(host, 7777)) {
            c.send(new PublishNewsCommand(
                    "jobs",
                    "POST System Programmer, knowledge in C++, Java and Python required. call 0x134693F"));
            System.out.println("msg1 sent");
            c.receive(); //ok

            c.send(new PublishNewsCommand(
                    "headlines",
                    "POST new SPL assignment is out soon!!"));
            System.out.println("msg2 sent");

            c.receive(); //ok

            c.send(new PublishNewsCommand(
                    "headlines",
                    "POST THE CAKE IS A LIE!"));
            System.out.println("msg3 sent");

            c.receive(); //ok
        }

    }

    private static void runSecondClient(String host) throws Exception {
        try (RCIClient c = new RCIClient(host, 7777)) {
            c.send(new FetchNewsCommand("jobs"));
            System.out.println("second client received: " + c.receive());
        }
    }

    private static void runThirdClient(String host) throws Exception {
        try (RCIClient c = new RCIClient(host, 7777)) {
            c.send(new FetchNewsCommand("headlines"));
            System.out.println("third client received: " + c.receive());
        }
    }
}
