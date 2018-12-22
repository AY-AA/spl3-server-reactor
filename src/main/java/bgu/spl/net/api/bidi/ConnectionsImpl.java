package bgu.spl.net.api.bidi;

import bgu.spl.net.srv.bidi.ConnectionHandler;
import java.util.HashMap;

/**
 * Connections maps an unique ID for each client connected to the server.
 * @param <T>
 */
public class ConnectionsImpl<T> implements Connections<T> {


    // each Integer represents an unique client's ID and its value is the client's connection handler
    private HashMap<Integer, ConnectionHandler> _connectionHandlers;


    public ConnectionsImpl()
    {
        //load _connectionsHAndler
        _connectionHandlers = new HashMap<>();
    }

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
