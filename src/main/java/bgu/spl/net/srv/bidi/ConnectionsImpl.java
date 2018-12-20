package bgu.spl.net.srv.bidi;

import bgu.spl.net.api.bidi.Connections;

import java.util.HashMap;
import java.util.List;

/**
 * Connections maps an unique ID for each client connected to the server.
 * @param <T>
 */
public class ConnectionsImpl<T> implements Connections<T> {


    // TODO : change data structure ?
    HashMap<Integer,ConnectionHandler> _connectionHandlers;

    // TODO : add support for disconnected users such as msg delivery even though they're logged off

    @Override
    public boolean send(int connectionId, T msg)
    {
        ConnectionHandler connectionHandler = _connectionHandlers.get(connectionId);
        if (connectionHandler != null) {
            connectionHandler.send(msg);
            return true;
        }
        return false;
    }

    @Override
    public void broadcast(T msg) {
        for (ConnectionHandler connectionHandler : _connectionHandlers.values())
            connectionHandler.send(msg);
    }

    @Override
    public void disconnect(int connectionId) {
        _connectionHandlers.remove(connectionId);
    }
}
