package bgu.spl.net.srv;

import bgu.spl.net.impl.newsfeed.NewsFeedClientMain;
import bgu.spl.net.impl.newsfeed.NewsFeedServerMain;

import javax.print.DocFlavor;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Vector;

public class TMP {
    public static void main(String []args) throws Exception {
        NewsFeedServerMain serverMain = new NewsFeedServerMain();
        Thread t = new Thread(serverMain);
        t.start();
        Thread.currentThread().sleep(2000);
//        NewsFeedServerMain.startServer(null);
        NewsFeedClientMain.main(null);

        byte _delimiter = '\0';
        System.out.println(_delimiter);

//        String a = "1 I follow follow ok ok ok";
//        String ans = a.replaceAll("follow","");
//        ans = ans.replaceAll("  "," ");
//        int opcode = Integer.parseInt(a.substring(0,1));
//        System.out.println(opcode);
    }
}
