package bgu.spl.net.srv.bidi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerDB{

    // A data structure to hold all usernames and their ids
    private ConcurrentHashMap<String,Integer> _usernamesIds;

    // A data structure to hold all usernames id and their awaiting public msgs which were retrieved while they were offline
    private HashMap<Integer, BlockingQueue<String>> _usernamesAwaitingPublicMsgs;

    // A data structure to hold all usernames id and their awaiting pm msgs which were retrieved while they were offline
    private HashMap<Integer, BlockingQueue<String>> _usernamesAwaitingPmMsgs;

    // A data structure to hold all server username id and its DB username id
    private HashMap<Integer,Integer> _serverDatabaseID;

    // A data structure to hold all usernames id and the number of posted msgs
    private HashMap<Integer,Integer> _numOfMsgsSentByUser;

    // A data structure to hold all usernames and passwords
    private HashMap<String,String> _usernamePassword;

    // A data structure to hold all usernames following
    private HashMap<Integer, List<Integer>> _followings;

    // A data structure to hold all usernames followers
    private HashMap<Integer, List<Integer>> _followers;

    private AtomicInteger _newestId;

    public ServerDB()
    {
        _usernamesAwaitingPublicMsgs = new HashMap<>();
        _usernamesIds = new ConcurrentHashMap<>();
        _usernamePassword = new HashMap<>();
        _followings = new HashMap<>();
        _followers = new HashMap<>();
        _usernamesAwaitingPmMsgs = new HashMap<>();
        _serverDatabaseID = new HashMap<>();
        _numOfMsgsSentByUser = new HashMap<>();
        _newestId = new AtomicInteger(0);
    }

    public int getId(String username)
    {
        Integer id = _usernamesIds.get(username);
        if (id != null)
            return id;
        return -1;
    }

    public void sendOfflineMsg(int senderUsernameId, String usernameToSend, String msg, boolean isPm)
    {
        Integer idToSend = _usernamesIds.get(usernameToSend);
        if (idToSend != null) {
            if (!isPm && !_usernamesAwaitingPublicMsgs.get(idToSend).contains(msg)) {
                _usernamesAwaitingPublicMsgs.get(idToSend).add(msg);
                _numOfMsgsSentByUser.put(senderUsernameId, _numOfMsgsSentByUser.get(senderUsernameId) + 1);
            } else if (isPm && _usernamesAwaitingPublicMsgs.get(idToSend) != null) {
                _usernamesAwaitingPmMsgs.get(idToSend).add(msg);
            }
        }
    }

    public void sendOfflineMsgWithID (int senderUsernameId, int usernameToSend, String msg)
    {
        if (_usernamesAwaitingPublicMsgs.get(usernameToSend) != null)
            if (!_usernamesAwaitingPublicMsgs.get(usernameToSend).contains(msg)) {
                _usernamesAwaitingPublicMsgs.get(usernameToSend).add(msg);
                _numOfMsgsSentByUser.put(senderUsernameId,_numOfMsgsSentByUser.get(senderUsernameId) +1);
            }
    }

    public String getAwaitingPublicMsg(int usernameId)
    {
        BlockingQueue queue = _usernamesAwaitingPublicMsgs.get(usernameId);
        if (queue != null)
            return (String)queue.poll();
        return null;
    }

    public String getAwaitingPmcMsg(int usernameId)
    {
        BlockingQueue queue = _usernamesAwaitingPmMsgs.get(usernameId);
        if (queue != null)
            return (String)queue.poll();
        return null;
    }


    public int register(int serverId, String username, String password)
    {
        if (_usernamePassword.containsKey(username))
            return -1;

        int lastKnown = _newestId.get();
        while (!_newestId.compareAndSet(lastKnown,lastKnown+1))
            lastKnown = _newestId.get();

        // data structures init
        int userID = lastKnown +1;
        _serverDatabaseID.put(serverId,userID);
        _usernamesIds.put(username,userID);
        _numOfMsgsSentByUser.put(userID,0);
        _usernamesAwaitingPmMsgs.put(userID, new LinkedBlockingQueue<>());
        _usernamesAwaitingPublicMsgs.put(userID, new LinkedBlockingQueue<>());
        _usernamePassword.put(username,password);
        _followings.put(userID,new ArrayList<>());
        _followers.put(userID,new ArrayList<>());
        return userID;
    }

    public int login(int serverId, String username, String password)
    {
        if (_usernamePassword.containsKey(username)){
            if (_usernamePassword.get(username).equals(password)){
                int dbID = _usernamesIds.get(username);
                _serverDatabaseID.put(serverId,dbID);
                return dbID;
            }
            return -2;
        }
        return -1;
    }

    public void disconnect(int serverID){
        _serverDatabaseID.remove(serverID);
    }

    public boolean checkFollowAndFollow (int user, String follow)
    {
        Integer userToFollowId = _usernamesIds.get(follow);
        if (userToFollowId != null && _followings.get(user).contains(userToFollowId))
            return true;
        _followings.get(user).add(userToFollowId);
        _followers.get(userToFollowId).add(user);
        return false;
    }

    public boolean checkUnfollowAndUnfollow (int user, String follow)
    {
        Integer userToFollowId = _usernamesIds.get(follow);
        if (userToFollowId != null && !_followings.get(user).contains(userToFollowId))
            return true;
        _followings.get(user).remove(userToFollowId);
        _followers.get(userToFollowId).remove(user);
        return false;
    }

    public String getRegisteredUsers() {
        StringBuilder ansBuilder = new StringBuilder();
        for (String currUser : _usernamesIds.keySet())
            ansBuilder.append(currUser);
        String users = ansBuilder.toString().trim();
        int numOfSpaces = 0;
        for (int i = 0; i < users.length(); i++)
        {
            if (Character.isWhitespace(users.charAt(0)))
                numOfSpaces++;
        }
        if (users.length() > 0)
            numOfSpaces ++;
        ansBuilder = new StringBuilder();
        ansBuilder.append("" + numOfSpaces + " ");
        ansBuilder.append(users);

        return ansBuilder.toString();
    }

    public int getNumOfPostsByUser( int user)
    {
        if (_numOfMsgsSentByUser.get(user) != null)
            return _numOfMsgsSentByUser.get(user);
        return -1;

    }

    public int getNumOfFollowers (int user)
    {
        if (_followers.get(user) != null)
            return _followers.get(user).size();
        return -1;
    }

    public int getNumOfFollowing(int usernameId) {
        if (_followings.get(usernameId) != null)
            return _followers.get(usernameId).size();
        return 1;
    }

    public List<Integer> getFollowers(int id) {
        return _followers.get(id);
    }
}
