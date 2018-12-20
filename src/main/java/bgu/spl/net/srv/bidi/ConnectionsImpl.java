package bgu.spl.net.srv.bidi;

import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.srv.bidi.ConnectionHandler;

import java.util.List;

/**
 * Connections maps a unique ID for each client connected to the server.
 * @param <T>
 */
public class ConnectionsImpl<T> implements Connections<T> {

    List<ConnectionHandler> _connectionHandlerList;


    @Override
    public boolean send(int connectionId, T msg) {
        return false;
    }

    @Override
    public void broadcast(T msg) {

    }

    @Override
    public void disconnect(int connectionId) {

    }
}
