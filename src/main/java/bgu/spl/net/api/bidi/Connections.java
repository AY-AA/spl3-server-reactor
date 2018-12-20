package bgu.spl.net.api.bidi;

import java.io.IOException;

public interface Connections<T> {


    /**
     * sends message 'msg' to client 'connectionId', using ConnectionHandler 'send' method.
     * @param connectionId
     * @param msg
     * @return
     */
    boolean send(int connectionId, T msg);

    /**
     * sends a message msg to all active and not logged-in clients, using ConnectionHandler 'send' method.
     * @param msg
     */
    void broadcast(T msg);

    /**
     * removes active client connectionId from server
     * @param connectionId
     */
    void disconnect(int connectionId);
}