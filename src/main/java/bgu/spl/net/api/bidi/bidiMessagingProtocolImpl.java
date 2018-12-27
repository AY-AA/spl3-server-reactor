package bgu.spl.net.api.bidi;

import bgu.spl.net.srv.bidi.ServerDB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class bidiMessagingProtocolImpl implements BidiMessagingProtocol<bidiMessages.bidiMessage> {
    public static int a= 0;

    private int _serverId, _dbId;
    private Connections<bidiMessages.bidiMessage> _connections;
    private boolean _shouldTerminate, _loggedIn;
    private ServerDB _database;
    private String _username;

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
            case REGISTER:     { if (checkLogged(opcodeCommand)) register(message);     break;     }
            case LOGIN:        { if (checkLogged(opcodeCommand)) login(message);        break;     }
            case LOGOUT:       { if (checkLogged(opcodeCommand)) logout(message);       break;     }
            case FOLLOW:       { if (checkLogged(opcodeCommand)) follow(message);       break;     }
            case POST:         { if (checkLogged(opcodeCommand)) post(message);         break;     }
            case PM:           { if (checkLogged(opcodeCommand)) pm(message);           break;     }
            case USERLIST:     { if (checkLogged(opcodeCommand)) userlist(message);     break;     }
            case STAT:         { if (checkLogged(opcodeCommand)) stat(message);         break;     }
            // TODO : remove notification, ack and error
            case NOTIFICATION: {  notification(message); break;     }
            case ACK:          {  ack(message);          break;     }
            case ERROR:        {  error(message);        break;     }
            default:           {                         return;    }



        }
    }

    private boolean checkLogged(OpcodeCommand opcodeCommand) {
        boolean canContinue = true;
        switch (opcodeCommand){
            case REGISTER:     {
                if (_loggedIn) {
                    sendError("1");
                    canContinue = false;
                }
                break;
            }
            case LOGIN:        {
                if (_loggedIn) {
                    sendError("2");
                    canContinue = false;
                }
                break;
            }
            case LOGOUT:       {
                if (!_loggedIn) {
                    sendError("3");
                    canContinue = false;
                }
                break;
            }
            case FOLLOW:       {
                if (!_loggedIn) {
                    sendError("4");
                    canContinue = false;
                }
                break;
            }
            case POST:         {
                if (!_loggedIn) {
                    sendError("5");
                    canContinue = false;
                }
                break;
            }
            case PM:           {
                if (!_loggedIn) {
                    sendError("6");
                    canContinue = false;
                }
                break;
            }
            case USERLIST:     {
                if (!_loggedIn) {
                    sendError("7");
                    canContinue = false;
                }
                break;
            }
            case STAT:         {
                if (!_loggedIn) {
                    sendError("8");
                    canContinue = false;
                }
                break;
            }
            case NOTIFICATION: {
                if (!_loggedIn) {
                    canContinue = false;
                }
                break;
            }
        }
        return canContinue;
    }

    @Override
    public boolean shouldTerminate() {
        return _shouldTerminate;
    }

    private void register(bidiMessages.bidiMessage message) {
        String username = message.getRelevantInfo().get(0);
        String password = message.getRelevantInfo().get(1);

        int dbResponse = _database.register(_serverId,username,password);
        if (dbResponse == -1)
            sendError("1");
        else {
            sendACK("1");
        }
    }

    private void login(bidiMessages.bidiMessage message) {
        String username = message.getRelevantInfo().get(0);
        String password = message.getRelevantInfo().get(1);
        int dbResponse = _database.login(_serverId,username,password);
        if (dbResponse == -1)
            sendError("2");
        else {
            _loggedIn = true;
            _dbId = dbResponse;
            sendACK("2");
            _username = username;
            sendAwaitingMsgs();
        }
    }

    private void sendAwaitingMsgs() {
        bidiMessages.bidiMessage missingMsg = _database.getAwaitingMsg(_dbId);
        while (missingMsg != null) {
            _connections.send(_serverId,missingMsg);
            missingMsg = _database.getAwaitingMsg(_dbId);
        }
    }

    private void logout(bidiMessages.bidiMessage message) {
        sendACK("3");
        _connections.disconnect(_serverId);
        _loggedIn = false;
        _database.disconnect(_serverId);
        _shouldTerminate = true;
    }

    private void follow(bidiMessages.bidiMessage message) {
        List<String> info = message.getRelevantInfo();
        int followUnfollowInt = Integer.parseInt(info.get(0));
        int numOfUsers = Integer.parseInt(info.get(1));
        int numOfFailures = 0;
        ArrayList<String> successfulAtemptsUsername = new ArrayList<>();
        for (int i = 2 ; i< 2 + numOfUsers ; i++){
            String currUsername = info.get(i);
            if (followUnfollowInt == 0) // follow
            {
                if(_database.checkFollowAndFollow(_dbId,currUsername))
                    numOfFailures++;
                else
                    successfulAtemptsUsername.add(currUsername);
            }
            else                        // unfollow
            {
                if(_database.checkUnfollowAndUnfollow(_dbId,currUsername))
                    numOfFailures++;
                else
                    successfulAtemptsUsername.add(currUsername);
            }
        }
        if (numOfFailures == numOfUsers)
            sendError("4");
        else
        {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("4 " + (numOfUsers - numOfFailures) + " ");    //num of successful attempts
            for (String username : successfulAtemptsUsername)
                stringBuilder.append(username + " ");                   //insert all username successfully followed/unfollowed
            String ackString = stringBuilder.toString().trim();
            sendACK(ackString);
        }
    }

    private void post(bidiMessages.bidiMessage message) {
        List<String> info = message.getRelevantInfo();
        String msg = info.get(0);       // msg index is 0 and usernames index is i > 0
        Set<Integer> sendTo = _database.getFollowers(_dbId);
        if (info.size() > 1) {          // has more users to send
            for (int i =1 ; i< info.size(); i++) {      // finds all usernames appear in message after '@'
                String currUser = info.get(i);
                sendTo.add(_database.getId(currUser));
            }
        }   //sends the message to all users
        for (Integer currUserDBID : sendTo) {
            int currUserServerID = _database.getServerID(currUserDBID);
            if (!_connections.send(currUserServerID, createNotification(msg,false)))
                _database.sendOfflineMsg(currUserDBID, createNotification(msg, false));
        }
        _database.addPostCount(_dbId);
        sendACK("5");
    }

    private void pm(bidiMessages.bidiMessage message) {
        String username = message.getRelevantInfo().get(0);
        String content = message.getRelevantInfo().get(1);
        int usernameDBID = _database.getId(username);
        int usernameServerID = _database.getServerID(usernameDBID);
        if (usernameDBID == -1) {     // there's no such username
            sendError("6");
            return;
        }
        else if (!_connections.send(usernameServerID,createNotification(content, true))) {
            _database.sendOfflineMsg(usernameDBID, createNotification(content, true));
        }
        sendACK("6");
    }

    private void userlist(bidiMessages.bidiMessage message) {

        String registeredUsersString = _database.getRegisteredUsers();
        sendACK("7 " + registeredUsersString);
    }

    private void stat(bidiMessages.bidiMessage message) {
        String username = message.getRelevantInfo().get(0);
        int usernameId = _database.getId(username);
        if (usernameId == -1) {
            sendError("8");
            return;
        }
        int posts = _database.getNumOfPostsByUser(usernameId);
        int followers = _database.getNumOfFollowers(usernameId);
        int following = _database.getNumOfFollowing(usernameId);
        String intsToString = posts + " " + followers + " " + following;
        sendACK("8 " + intsToString);
    }

    private void sendError(String s) {
        bidiMessages.bidiMessage msg = new bidiMessages.bidiMessage("ERROR " + s);
        _connections.send(_serverId,msg);
    }

    private void sendACK(String s) {
        bidiMessages.bidiMessage msg = new bidiMessages.bidiMessage("ACK " + s);
        _connections.send(_serverId,msg);
    }

    private bidiMessages.bidiMessage createNotification(String msg, boolean isPm) {
        StringBuilder stringBuilder = new StringBuilder();
        if (isPm)
            stringBuilder.append("9 0 ");
        else
            stringBuilder.append("9 1 ");
        stringBuilder.append(_username + " " + msg);
        return new bidiMessages.bidiMessage(stringBuilder.toString());
    }












    // TODO : delete it --- > needs to be in client only!
    private void notification(bidiMessages.bidiMessage message) {
        System.out.println("REACHED HERE notification");
//        String pmPublic = message.getRelevantInfo().get(0);
//        String username = message.getRelevantInfo().get(1);
//        String content = message.getRelevantInfo().get(2);
//        boolean isPm = pmPublic.equals("PM");
//        int usernameId = _database.getId(username);
//        if (usernameId != -1 && !_connections.send(usernameId,message))
//        {
//            System.out.println("new notification :");
//            System.out.println(message.getMsgToSend());
////            _database.sendOfflineMsg(_dbId,username,content,isPm);
//        }

    }

    // TODO : delete it --- > needs to be in client only!
    private void ack(bidiMessages.bidiMessage message) {
        System.out.println("REACHED HERE ack");
//
//        StringBuilder stringBuilder = new StringBuilder();
//        List<String> stringList = message.getRelevantInfo();
//
//        for (String currString : stringList)
//            stringBuilder.append(currString + " ");
//
//        String result = stringBuilder.toString();
//        System.out.println(result);

    }

    // TODO : delete it --- > needs to be in client only!
    private void error(bidiMessages.bidiMessage message) {
        System.out.println("REACHED HERE error");
//
//        String result = message.getRelevantInfo().get(0);
//        System.out.println(result);


    }



}
