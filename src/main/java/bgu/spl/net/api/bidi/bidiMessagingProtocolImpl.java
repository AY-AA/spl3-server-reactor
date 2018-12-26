package bgu.spl.net.api.bidi;

import bgu.spl.net.srv.bidi.ServerDB;

import java.util.HashMap;
import java.util.List;

public class bidiMessagingProtocolImpl implements BidiMessagingProtocol<bidiMessages.bidiMessage> {

    private int _serverId, _dbId;
    private Connections<bidiMessages.bidiMessage> _connections;
    private boolean _shouldTerminate, _loggedIn;
    private ServerDB _database;

    public bidiMessagingProtocolImpl(ServerDB _database) {
        this._database = _database;
        _dbId = _serverId = -1;
    }

    @Override
    public void start(int connectionId, Connections<bidiMessages.bidiMessage> connections) {
        _serverId = connectionId;
        _connections = connections;
        _loggedIn = _shouldTerminate = false;
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
//        if (_loggedIn){
//            error;;;
//        return;
//    }
        String username = message.getRelevantInfo().get(0);
        String password = message.getRelevantInfo().get(1);

        int dbResponse = _database.register(_serverId,username,password);
        //TODO : impl error msgs
//        else if (dbResponse == -1)
//            already registered ERROR;
//        else
//              _dbId = dbResponse;

    }

    private void login(bidiMessages.bidiMessage message) {
        //        if (_loggedIn){
//            error;;;
//        return;
//    }
        String username = message.getRelevantInfo().get(0);
        String password = message.getRelevantInfo().get(1);
        int dbResponse = _database.login(_serverId,username,password);
        //TODO : impl error msgs
//        if (dbResponse == -2)
//            pw doesnt match ERROR;
//        else if (dbResponse == -1)
//            no such user ERROR;
//        else _dbId = dbResponse;
//        _
    }

    private void logout(bidiMessages.bidiMessage message) {
        if (_loggedIn) {
            _connections.disconnect(_serverId);
            _database.disconnect(_serverId);
            _loggedIn = false;
            _shouldTerminate = true;
        }
//        else
//            ERROR;
    }

    private void follow(bidiMessages.bidiMessage message) {
        //        if (!_loggedIn){
//            error;;;
//        return;
        //TODO : impl

        List<String> info = message.getRelevantInfo();
        int followUnfollowInt = Integer.parseInt(info.get(0));
        int numOfUsers = Integer.parseInt(info.get(1));
        int numOfErrors = 0;
        for (int i = 2 ; i< 2 + numOfUsers ; i++){
            String currUsername = info.get(i);
            if (followUnfollowInt == 0) // follow
            {
                if(_database.checkFollowAndFollow(_dbId,currUsername))
                    numOfErrors++;
            }
            else                        // unfollow
            {
                if(_database.checkUnfollowAndUnfollow(_dbId,currUsername))
                    numOfErrors++;
            }
        }
        // TODO : impl
//        if (numOfErrors == numOfUsers)
//            ERROR;
    }

    private void post(bidiMessages.bidiMessage message) {
        //TODO : impl
        //        if (!_loggedIn){
//            error;;;
//        return;

        List<String> info = message.getRelevantInfo();
        String msg = info.get(0);
        if (info.size() > 1) {        // has more users to send
            for (int i =1 ; i< info.size(); i++)
            {
                String currUser = info.get(i);
                int currUserId = _database.getId(currUser);
                if (currUserId != -1) {
                    if (!_connections.send(_dbId, message))
                        _database.sendOfflineMsg(_dbId, currUser, msg,false);
                }
            }
        }
        List<Integer> followers = _database.getFollowers(_dbId);
        for (Integer currUser : followers)
            if (!_connections.send(currUser,message))
                _database.sendOfflineMsgWithID(_dbId,currUser,msg);
    }

    private void pm(bidiMessages.bidiMessage message) {
        //TODO : impl
        //        if (!_loggedIn){
//            error;;;
//        return;

        String username = message.getRelevantInfo().get(0);
        String content = message.getRelevantInfo().get(1);
        if (!_connections.send(_dbId,message))
            _database.sendOfflineMsg(_dbId,username,content,true);

    }

    private void userlist(bidiMessages.bidiMessage message) {
        //TODO : impl
        //        if (!_loggedIn){
//            error;;;
//        return;

        String registeredUsers = _database.getRegisteredUsers();

        bidiMessages.bidiMessage msgToSend = new bidiMessages.bidiMessage("ACK 7 " + registeredUsers);
        _connections.send(_dbId,msgToSend);

    }

    private void stat(bidiMessages.bidiMessage message) {
        //TODO : impl
        //        if (!_loggedIn){
//            error;;;
//        return;

        String username = message.getRelevantInfo().get(0);
        int usernameId = _database.getId(username);
        //TODO : impl if only!
//        if (usernameId == -1)
//            ERROR
//        else{
//            int posts = _database.getNumOfPostsByUser(usernameId);
//            int followers = _database.getNumOfFollowers(usernameId);
//            int following = _database.getNumOfFollowing(usernameId);
//            if (posts != -1 && followers != -1 && following != -1)
//            {
//                String result = "" + posts + " " + followers + " " + following;
//                bidiMessages.bidiMessage msgToSend = new bidiMessages.bidiMessage("ACK 8 " + result);
//            }
//            // TODO : delete after testing
//            else    // won't happen
//            {
//                System.out.println("ERROR: bidiMessagingProtocolImpl: line 170, args = -1");
//            }
//        }

    }

    private void notification(bidiMessages.bidiMessage message) {
        //        if (!_loggedIn){
//            error;;;
//        return;
        String pmPublic = message.getRelevantInfo().get(0);
        String username = message.getRelevantInfo().get(1);
        String content = message.getRelevantInfo().get(2);
        boolean isPm = pmPublic.equals("PM");
        int usernameId = _database.getId(username);
        if (usernameId != -1 && !_connections.send(usernameId,message))
        {
            _database.sendOfflineMsg(_dbId,username,content,isPm);
        }
        // TODO imp ERROR
//        else if (usernameId == -1 )
//            ERROR
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
