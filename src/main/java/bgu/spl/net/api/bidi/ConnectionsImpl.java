package bgu.spl.net.api.bidi;

import bgu.spl.net.srv.bidi.ConnectionHandler;
import java.util.HashMap;

/**
 * Connections maps an unique ID for each client connected to the server.
 */
public class ConnectionsImpl<T> implements Connections<T> {

    // each Integer represents an unique client's ID and its value is the client's connection handler
    private HashMap<Integer, ConnectionHandler<T>> _connectionHandlers;


    public ConnectionsImpl()
    {
        _connectionHandlers = new HashMap<>();
    }


    @Override
    public boolean send(int connectionId, T msg)
    {
        ConnectionHandler connectionHandler = _connectionHandlers.get(connectionId);
        if (connectionHandler != null && msg != null) {
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


    public void addConnection(int connectionId, ConnectionHandler<T> connectionHandler){
        _connectionHandlers.put(connectionId,connectionHandler);
    }



}
