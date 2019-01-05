package bgu.spl.net.srv.bidi;

import bgu.spl.net.api.bidi.bidiMessage;

import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerDB{

    // A data structure to hold all usernames and their ids
    private ConcurrentHashMap<String,Integer> _usernamesIds;

    // A data structure to hold all usernames id and their awaiting public msgs which were retrieved while they were offline
    private ConcurrentHashMap<Integer, BlockingQueue<bidiMessage>> _usernamesAwaitingMsgs;

    // A data structure to hold all server username id and its DB username id
    private ConcurrentHashMap<Integer,Integer> _serverDatabaseID;

    // A data structure to hold all usernames id and the number of posted msgs
    private ConcurrentHashMap<Integer,Integer> _numOfMsgsSentByUser;

    // A data structure to hold all usernames and passwords
    private ConcurrentHashMap<String,String> _usernamePassword;

    // A data structure to hold all usernames following
    private ConcurrentHashMap<Integer, ConcurrentSkipListSet<Integer>> _followings;

    // A data structure to hold all usernames followers
    private ConcurrentHashMap<Integer, ConcurrentSkipListSet<Integer>> _followers;

    private AtomicInteger _newestId;

    private ConcurrentSkipListSet<String> _loggedInUsers;

    public ServerDB()
    {
        _usernamesAwaitingMsgs = new ConcurrentHashMap<>();
        _usernamesIds = new ConcurrentHashMap<>();
        _usernamePassword = new ConcurrentHashMap<>();
        _followings = new ConcurrentHashMap<>();
        _followers = new ConcurrentHashMap<>();
//        _usernamesAwaitingPmMsgs = new HashMap<>();
        _serverDatabaseID = new ConcurrentHashMap<>();
        _numOfMsgsSentByUser = new ConcurrentHashMap<>();
        _newestId = new AtomicInteger(0);
        _loggedInUsers = new ConcurrentSkipListSet<>();
    }

    public int getId(String username)
    {
        Integer id = _usernamesIds.get(username);
        if (id != null)
            return id;
        return -1;
    }

    public int register(int serverId, String username, String password)
    {
//        if (_usernamePassword.containsKey(username))
//            return -1;

        if (_usernamePassword.putIfAbsent(username,password) != null)
            return -1;

        int lastKnown = _newestId.get();
        while (!_newestId.compareAndSet(lastKnown,lastKnown+1))
            lastKnown = _newestId.get();

        // data structures init
        int userID = lastKnown +1;
        _serverDatabaseID.put(serverId,-2);
        _usernamesIds.put(username,userID);
        _numOfMsgsSentByUser.put(userID,0);
        _usernamesAwaitingMsgs.put(userID, new LinkedBlockingQueue<>());
//        _usernamesAwaitingPublicMsgs.put(userID, new LinkedBlockingQueue<>());
//        _usernamePassword.put(username,password);
        _followings.put(userID,new ConcurrentSkipListSet());
        _followers.put(userID,new ConcurrentSkipListSet<>());
        return userID;
    }

    public int login(int serverId, String username, String password)
    {
        if (_usernamePassword.containsKey(username)){
            if (_usernamePassword.get(username).equals(password)){
                int dbID = _usernamesIds.get(username);
                _serverDatabaseID.remove(dbID,-2);
                if (_loggedInUsers.add(username)) {
                    _serverDatabaseID.putIfAbsent(serverId, dbID);
                    return dbID;
                }
            }
        }
        return -1;
    }

    public void disconnect(int serverID, String username){
      //  _usernamesIds.put(username, -2);
        _serverDatabaseID.remove(serverID);
        _loggedInUsers.remove(username);
    }

    public boolean checkFollowAndFollow (int user, String follow)
    {
        Integer userToFollowId = _usernamesIds.get(follow);
        if (userToFollowId == null || _followings.get(user).contains(userToFollowId))
            return true;

        _followings.get(user).add(userToFollowId);
        _followers.get(userToFollowId).add(user);
        return false;
    }

    public boolean checkUnfollowAndUnfollow (int user, String follow)
    {
        Integer userToFollowId = _usernamesIds.get(follow);
        if (userToFollowId == null || !_followings.get(user).contains(userToFollowId))
            return true;
        _followings.get(user).remove(userToFollowId);
        _followers.get(userToFollowId).remove(user);
        return false;
    }

    public String getRegisteredUsers() {
        StringBuilder ansBuilder = new StringBuilder();
        for (String currUser : _usernamesIds.keySet())
            ansBuilder.append(currUser + " ");
        String tmp = ansBuilder.toString().trim();
        int numOfUsers = 0;                 // count number of spaces this way
        while (tmp.contains(" ")){          // of implementation is because someone can
            numOfUsers++;                   // register while the for loop ends and then size changes
            tmp = tmp.substring(tmp.indexOf(" ") +1);
        }
        if (!tmp.isEmpty())
            numOfUsers++;
        String ans = numOfUsers + " " + ansBuilder.toString().trim();
        return ans;
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
            return _followings.get(usernameId).size();
        return 1;
    }

    public Set<Integer> getFollowers(int id) {
        return _followers.get(id);
    }

    public int getServerID(int dbID)
    {
        if (dbID == -1)
            return -1;
        for (Integer databaseId : _serverDatabaseID.keySet()){
            if (_serverDatabaseID.get(databaseId) == dbID)
                return databaseId;
        }
        return -1;
    }

    public void addPostCount(int dbId) {
        _numOfMsgsSentByUser.put(dbId, _numOfMsgsSentByUser.get(dbId) + 1);
    }


    public void sendOfflineMsg(int usernameToSend, bidiMessage notification) {
        if (!_usernamesAwaitingMsgs.get(usernameToSend).contains(notification)){
            //msg was already sent
            // it can happen in case username follows and gets this message
            // because '@username' is included inside the msg
            _usernamesAwaitingMsgs.get(usernameToSend).add(notification);
        }
    }

    public bidiMessage getAwaitingMsg(int dbId) {
        if (_usernamesAwaitingMsgs.get(dbId).isEmpty())
            return null;
        return _usernamesAwaitingMsgs.get(dbId).poll();
    }
}
