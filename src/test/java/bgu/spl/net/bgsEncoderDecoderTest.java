package bgu.spl.net;

import bgu.spl.net.api.bidi.bidiMessage;
import bgu.spl.net.api.bidi.bidiMessageEncoderDecoder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class bgsEncoderDecoderTest {
    static bidiMessageEncoderDecoder decoder = new bidiMessageEncoderDecoder();

    static Map<byte[], String> a = new LinkedHashMap<>();
    private static final Map<String, String> myMap;

    static {
        myMap = new HashMap<String, String>();
        myMap.put("00 04 01 00 01 53 68 76 61 74 00", "FOLLOW 1 1 Shvat");
        myMap.put("00 04 01 00 02 49 6e 62 61 e2 80 99 72 00 61 31 32 33 00", "FOLLOW 1 2 Inba’r a123");
        myMap.put("00 04 00 00 02 4e 65 79 6d 61 00 61 31 32 e2 80 99 33 00", "FOLLOW 0 2 Neyma a12’3");
        myMap.put("00 04 01 00 03 41 6d 69 74 00 61 31 32 33 00 79 75 76 61 e2 80 99 6c 00",
                  "FOLLOW 1 3 Amit a123 yuva’l");
        myMap.put("00 01 41 6c 6f 6e e2 80 99 61 00 61 31 32 e2 80 99 33 00", "REGISTER Alon’a a12’3");
        myMap.put("00 02 d7 9e d7 93 d7 94 d7 99 d7 9d 21 e2 80 99 79 00 61 31 32 e2 80 99 33 00", "LOGIN מדהים!’y a12’3");
        myMap.put("00 03", "LOGOUT");
        myMap.put("00 08 4d 6f 72 74 e2 80 99 79 00", "STAT Mort’y");
        myMap.put("00 06 4d 6f 72 74 e2 80 99 79 00 4e 6f 62 6f 64 79 20 65 78 69 73 74 73 20 6f 6e 20 70 75 72 70 6f 73 65 2c 20 6e 6f 62 6f 64 79 20 62 65 6c 6f 6e 67 73 20 61 6e 79 77 68 65 72 65 2c 20 65 76 65 72 79 62 6f 64 79 e2 80 99 73 20 67 6f 6e 6e 61 20 64 69 65 2e 20 43 6f 6d 65 20 77 61 74 63 68 20 54 56 2e 00", "PM Mort’y Nobody exists on purpose, nobody belongs anywhere, everybody’s gonna die. Come watch TV.");
        myMap.put(
                "00 05 4e 6f 62 6f 64 79 20 65 78 69 73 74 73 20 6f 6e 20 70 75 72 70 6f 73 65 2c 20 6e 6f 62 6f 64 79 20 62 65 6c 6f 6e 67 73 20 61 6e 79 77 68 65 72 65 2c 20 65 76 65 72 79 62 6f 64 79 e2 80 99 73 20 67 6f 6e 6e 61 20 64 69 65 2e 20 43 6f 6d 65 20 77 61 74 63 68 20 54 56 2e 00",
                "POST Nobody exists on purpose, nobody belongs anywhere, everybody’s gonna die. Come watch TV.");
        for (Map.Entry<String, String> entry : myMap.entrySet()) {
            String input = entry.getKey();
            String[] tmparr = input.split(" ");
            byte[] bytes = new byte[tmparr.length];
            for (int i = 0; i < tmparr.length; i++) {
                try {
                    bytes[i] = (byte) ((Character.digit(tmparr[i].charAt(0), 16) << 4)
                                       + Character.digit(tmparr[i].charAt(1), 16));
                }
                catch (Exception e){
                    bytes[i] = (byte) ((Character.digit(tmparr[i].charAt(0), 16) << 4)
                                       + Character.digit(tmparr[i].charAt(1), 16));

                }
            }
            a.put(bytes, entry.getValue());
        }
    }

    @org.junit.Before
    public void setUp() throws Exception {

    }

    @org.junit.After
    public void tearDown() throws Exception {
    }

    @org.junit.Test
    public void decodeNextByte() {
        int kkk = 0;
        for (Map.Entry<byte[], String> entry : a.entrySet()) {
            byte[] bytes = entry.getKey();
            bidiMessage output = null;
            for (int i = 0; i < bytes.length; i++) {
                try {
                    output = decoder.decodeNextByte(bytes[i]);
                }
                catch (Exception e) {
                    System.out.println("kkk: " + kkk + ", i:" + i);
                    e.printStackTrace();
                    fail();
                }
                if (i < bytes.length - 1)
                    assertNull(output);
            }

            assertNotNull(kkk + " " + entry.getValue(), output.getString());
            assertEquals(getStringA(kkk + " " + entry.getValue()), getStringA(entry.getValue()), output.getString());
            kkk++;
        }
    }

    private String getStringA(String s) {
        s = s.trim();
        if (!s.isEmpty())
        {
            int indexOF = s.indexOf(" ");

            s = s.substring(indexOF + 1);
        }
        return s;
    }

    @org.junit.Test
    public void encode() {

    }
}