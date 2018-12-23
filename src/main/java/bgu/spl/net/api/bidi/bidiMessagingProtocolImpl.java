package bgu.spl.net.api.bidi;

import java.util.List;

public class bidiMessagingProtocolImpl implements BidiMessagingProtocol<bidiMessages.bidiMessage> {

    private int _id;
    private Connections<bidiMessages.bidiMessage> _connections;
    private boolean _shouldTerminate;

    @Override
    public void start(int connectionId, Connections<bidiMessages.bidiMessage> connections) {
        _id = connectionId;
        _connections = connections;
    }

    @Override
    public void process(bidiMessages.bidiMessage message) {
        if (message.getRelevantInfo() == null)
            return;
        OpcodeCommand opcodeCommand = message.getOpcode();
        switch (opcodeCommand){
            case REGISTER:     { register(message);     break;     }
            case LOGIN:        { login(message);        break;     }
            case LOGOUT:       { logout(message);       break;     }
            case FOLLOW:       { follow(message);       break;     }
            case POST:         { post(message);         break;     }
            case PM:           { pm(message);           break;     }
            case USERLIST:     { userlist(message);     break;     }
            case STAT:         { stat(message);         break;     }
            case NOTIFICATION: { notification(message); break;     }
            case ACK:          { ack(message);          break;     }
            case ERROR:        { error(message);        break;     }
            default:           {                        return;    }
        }
    }

    @Override
    public boolean shouldTerminate() {
        return _shouldTerminate;
    }

    private void register(bidiMessages.bidiMessage message) {
        String username = message.getRelevantInfo().get(0);
        String password = message.getRelevantInfo().get(1);
        // TODO: implement
    }

    private void login(bidiMessages.bidiMessage message) {
        String username = message.getRelevantInfo().get(0);
        String password = message.getRelevantInfo().get(1);
        // TODO: implement
    }

    private void logout(bidiMessages.bidiMessage message) {
        _connections.disconnect(_id);
        _shouldTerminate = true;
    }

    private void follow(bidiMessages.bidiMessage message) {
        List<String> info = message.getRelevantInfo();
        int followUnfollowInt = Integer.parseInt(info.get(0));
        int numOfUsers = Integer.parseInt(info.get(1));
        for (int i = 2 ; i< 2 + numOfUsers ; i++){
            String currUsername = info.get(i);
            if (followUnfollowInt == 1)
            {
                // TODO : implement
            }
            else
            {
                // TODO : implement
            }
        }
    }

    private void post(bidiMessages.bidiMessage message) {
        List<String> info = message.getRelevantInfo();
        String msg = info.get(0);
        if (info.size() > 1) {        // has more users to send
            for (int i =1 ; i< info.size(); i++)
            {
                String currUser = info.get(i);
                // TODO : implement msg sending
            }
        }
        // TODO : implement msg sending
    }

    private void pm(bidiMessages.bidiMessage message) {
        String username = message.getRelevantInfo().get(0);
        String content = message.getRelevantInfo().get(1);
        // TODO : implement

    }

    private void userlist(bidiMessages.bidiMessage message) {
        // TODO : implement

    }

    private void stat(bidiMessages.bidiMessage message) {
        String username = message.getRelevantInfo().get(0);
        // TODO : implement

    }

    private void notification(bidiMessages.bidiMessage message) {
        String pmPublic = message.getRelevantInfo().get(0);
        String username = message.getRelevantInfo().get(1);
        String content = message.getRelevantInfo().get(2);
        // TODO : implement

    }

    private void ack(bidiMessages.bidiMessage message) {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> stringList = message.getRelevantInfo();

        for (String currString : stringList)
            stringBuilder.append(currString + " ");

        String result = stringBuilder.toString();

        // TODO : implement

    }

    private void error(bidiMessages.bidiMessage message) {
        String result = message.getRelevantInfo().get(0);
        // TODO : implement

    }

}
