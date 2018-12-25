package bgu.spl.net.srv;

import bgu.spl.net.api.bidi.bidiMessageEncoderDecoder;
import bgu.spl.net.api.bidi.bidiMessages;
import bgu.spl.net.impl.newsfeed.NewsFeedClientMain;
import bgu.spl.net.impl.newsfeed.NewsFeedServerMain;

import javax.print.DocFlavor;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class TMP {
    public static void main(String []args) throws Exception {
//        NewsFeedServerMain serverMain = new NewsFeedServerMain();
//        Thread t = new Thread(serverMain);
//        t.start();
//        Thread.currentThread().sleep(200);
////        NewsFeedServerMain.startServer(null);
//        NewsFeedClientMain.main(null);
//
//        byte _delimiter = '\0';
//        System.out.println(_delimiter);




        bidiMessageEncoderDecoder encoderDecoder = new bidiMessageEncoderDecoder();
        String del = "\0";
        byte[] xx = encoderDecoder.encode(new bidiMessages.bidiMessage("FOLLOW 1 2 A LOGOUT"));
        bidiMessages.bidiMessage y = null;
        int i =0;
        while (y == null && i<xx.length){
            y = encoderDecoder.decodeNextByte(xx[i]);
            i++;
        }
//        byte _delimiter = '\0';
//        String aa = "\0";
//        byte[] aaa = aa.getBytes();

        System.out.println(y.getMsgToSend());
        List<String> a = y.getRelevantInfo();
        for (String st : a)
            System.out.println( st);
//        System.out.println(_delimiter == aaa[0]);


//
//        System.out.println(Short.BYTES);

//        ByteBuffer _opcode = ByteBuffer.allocate(4);
//        _opcode.put(xx[0]);
//
////        short aaa = 5;
////        byte[] a = shortToBytes(aaa);
//        System.out.println("aaaa");

//        String a = "1 I follow follow ok ok ok";
//        String ans = a.replaceAll("follow","");
//        ans = ans.replaceAll("  "," ");
//        int opcode = Integer.parseInt(a.substring(0,1));
//        System.out.println(opcode);
    }

    public static byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }

}
