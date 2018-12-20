package bgu.spl.net.api.bidi;

import bgu.spl.net.srv.bidi.ConnectionHandler;

import java.util.List;

public class ConnectionsImpl<T> implements Connections<T> {

    List<ConnectionHandler> _connectionHandlerList;

    @Override
    public boolean send(int connectionId, Object msg)
    {

        return false;
    }

    @Override
    public void broadcast(Object msg)
    {

    }

    @Override
    public void disconnect(int connectionId)
    {

    }
}
