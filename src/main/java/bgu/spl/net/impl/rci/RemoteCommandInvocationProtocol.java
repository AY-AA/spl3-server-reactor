package bgu.spl.net.impl.rci;

import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;

import java.io.Serializable;

public class RemoteCommandInvocationProtocol<T> implements BidiMessagingProtocol<String> {

    private T arg;

    private boolean _shouldTerminate = false;
    private Connections<String> _connections;
    private int _id ;

    public RemoteCommandInvocationProtocol(T arg) {
        this.arg = arg;
    }

    @Override
    public void start(int connectionId, Connections<String> connections) {
        _connections = connections;
        _id = connectionId;
    }

    @Override
    public void process(String msg) {
        // parse msg
        // if msg sends msg to a certain user, use _connections.send/broadcast
        // if returns false -> no such user or error.. send error msg to _id!

        // if log out -> shoutTerminate = true;

        return;
//        return ((Command) msg).execute(arg);

    }

    @Override
    public boolean shouldTerminate() {
        return _shouldTerminate;
    }

}