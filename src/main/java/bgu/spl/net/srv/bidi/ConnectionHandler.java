package bgu.spl.net.srv.bidi;

import java.io.Closeable;
import java.io.IOException;

public interface ConnectionHandler<T> extends Closeable{

    /**
     * Sends message msg to the client. Should be used by 'send' and 'broadcast' in Connections.
     * @param msg
     */
    void send(T msg) ;

}