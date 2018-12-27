package bgu.spl.net.srv;
import bgu.spl.net.api.bidi.bidiMessageEncoderDecoder;
import bgu.spl.net.api.bidi.bidiMessages;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

public class Client3 implements Closeable,Runnable {

    private final bidiMessageEncoderDecoder encdec;
    private final Socket sock;
    private final BufferedInputStream in;
    private final BufferedOutputStream out;

    public Client3(String host, int port) throws IOException {
        sock = new Socket(host, port);
        encdec = new bidiMessageEncoderDecoder();
        in = new BufferedInputStream(sock.getInputStream());
        out = new BufferedOutputStream(sock.getOutputStream());
    }

    public void send(bidiMessages.bidiMessage cmd) throws IOException {
        out.write(encdec.encode(cmd));
        out.flush();
    }

    public bidiMessages.bidiMessage receive() throws IOException {
        int read;
        while ((read = in.read()) >= 0) {
            bidiMessages.bidiMessage msg = encdec.decodeNextByte((byte) read);
            if (msg != null) {
                return msg;
            }
        }

        throw new IOException("disconnected before complete reading message");
    }

    @Override
    public void close() throws IOException {
        out.close();
        in.close();
        sock.close();
    }

    @Override
    public void run() {
        try {
            Thread.currentThread().sleep(2000);

            send(new bidiMessages.bidiMessage("LOGIN avishai 1111"));

            send(new bidiMessages.bidiMessage("STAT alex"));


//            send(new bidiMessages.bidiMessage("LOGOUT"));


//            bidiMessages.bidiMessage msg = receive();



//            System.out.println("avishai received : " + msg.getMsgToSend());

        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("FINISHED AVISHAI!");
        try {
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}