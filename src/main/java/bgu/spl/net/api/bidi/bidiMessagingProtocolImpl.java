package bgu.spl.net.api.bidi;

import bgu.spl.net.srv.bidi.ServerDB;

import java.util.List;

public class bidiMessagingProtocolImpl implements BidiMessagingProtocol<bidiMessages.bidiMessage> {

    private int _id;
    private Connections<bidiMessages.bidiMessage> _connections;
    private boolean _shouldTerminate;
    private ServerDB _database;

    public bidiMessagingProtocolImpl(ServerDB _database) {
        this._database = _database;
        _id = -1;
    }

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

        int dbResponse = _database.register(username,password);
        //TODO : impl error msgs
//        else if (dbResponse == -1)
//            already registered ERROR;
//        else
//            _id = dbResponse;
    }

    private void login(bidiMessages.bidiMessage message) {
        String username = message.getRelevantInfo().get(0);
        String password = message.getRelevantInfo().get(1);
        int dbResponse = _database.login(username,password);
        //TODO : impl error msgs
//        if (dbResponse == -2)
//            pw doesnt match ERROR;
//        else if (dbResponse == -1)
//            no such user ERROR;
//        else
//            _id = dbResponse;
    }

    private void logout(bidiMessages.bidiMessage message) {
        if (_id != -1)
            _connections.disconnect(_id);
        _shouldTerminate = true;
    }

    private void follow(bidiMessages.bidiMessage message) {
        //TODO : impl
//        if (_id == -1)
//            not connected ERROR;

        List<String> info = message.getRelevantInfo();
        int followUnfollowInt = Integer.parseInt(info.get(0));
        int numOfUsers = Integer.parseInt(info.get(1));
        int numOfErrors = 0;
        for (int i = 2 ; i< 2 + numOfUsers ; i++){
            String currUsername = info.get(i);
            if (followUnfollowInt == 0) // follow
            {
                if(_database.checkFollowAndFollow(_id,currUsername))
                    numOfErrors++;
            }
            else                        // unfollow
            {
                if(_database.checkUnfollowAndUnfollow(_id,currUsername))
                    numOfErrors++;
            }
        }
        // TODO : impl
//        if (numOfErrors == numOfUsers)
//            ERROR;
    }

    private void post(bidiMessages.bidiMessage message) {
        //TODO : impl
//        if (_id == -1)
//            not connected ERROR;

        List<String> info = message.getRelevantInfo();
        String msg = info.get(0);
        if (info.size() > 1) {        // has more users to send
            for (int i =1 ; i< info.size(); i++)
            {
                String currUser = info.get(i);
                int currUserId = _database.getId(currUser);
                if (currUserId != -1) {
                    if (!_connections.send(_id, message))
                        _database.sendOfflineMsg(_id, currUser, msg);
                }
            }
        }
        List<Integer> followers = _database.getFollowers(_id);
        for (Integer currUser : followers)
            if (!_connections.send(currUser,message))
                _database.sendOfflineMsgWithID(_id,currUser,msg);
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
