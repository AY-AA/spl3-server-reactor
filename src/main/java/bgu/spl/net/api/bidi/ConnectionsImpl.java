package bgu.spl.net.api.bidi;

import bgu.spl.net.srv.bidi.ConnectionHandler;
import java.util.HashMap;

/**
 * Connections maps an unique ID for each client connected to the server.
 */
public class ConnectionsImpl implements Connections<bidiMessages.bidiMessage> {


    // each Integer represents an unique client's ID and its value is the client's connection handler
    private HashMap<Integer, ConnectionHandler<String>> _connectionHandlers;


    public ConnectionsImpl()
    {
        //load _connectionsHAndler
        _connectionHandlers = new HashMap<>();
    }

    // TODO : add support for disconnected users such as msg delivery even though they're logged off

    @Override
    public boolean send(int connectionId, bidiMessages.bidiMessage msg)
    {
        ConnectionHandler connectionHandler = _connectionHandlers.get(connectionId);
        if (connectionHandler != null && msg != null) {
            connectionHandler.send(msg.getMsgToSend());
            return true;
        }
        return false;
    }

    @Override
    public void broadcast(bidiMessages.bidiMessage msg) {
        for (ConnectionHandler connectionHandler : _connectionHandlers.values())
            connectionHandler.send(msg.getMsgToSend());
    }

    @Override
    public void disconnect(int connectionId) {
        _connectionHandlers.remove(connectionId);
    }






}
