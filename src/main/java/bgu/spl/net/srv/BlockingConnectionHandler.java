package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.api.bidi.bidiMessage;
import bgu.spl.net.srv.bidi.ConnectionHandler;

public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<bidiMessage> {

    private final BidiMessagingProtocol<bidiMessage> protocol;
    private final MessageEncoderDecoder<bidiMessage> encdec;
    private final Socket sock;
    private final int ID;
    private final Connections CONNECTIONS;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private volatile boolean connected = true;

    public BlockingConnectionHandler(int id, Connections connections,Socket sock,
                                     MessageEncoderDecoder<bidiMessage> reader,
                                     BidiMessagingProtocol<bidiMessage> protocol) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
        CONNECTIONS = connections;
        ID = id;
    }

    @Override
    public void run() {
        try (Socket sock = this.sock) { //just for automatic closing
            int read;

            protocol.start(ID,CONNECTIONS);

            in = new BufferedInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());

            while (!protocol.shouldTerminate() && connected && (read = in.read()) >= 0) {
                bidiMessage nextMessage = encdec.decodeNextByte((byte) read);
                if (nextMessage != null) {
                    protocol.process(nextMessage);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void close() throws IOException {
        connected = false;
        sock.close();
    }

    @Override
    public void send(bidiMessage msg) {
        try {
            // TODO : REMOVE PRINTING
            System.out.println(msg.getString());
            out.write(encdec.encode(msg));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
