package bgu.spl.net.srv.bidi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerDB{

    // A data structure to hold all usernames and their ids
    private HashMap<String,Integer> _usernamesIds;

    // A data structure to hold all usernames id and their awaiting msgs which were retrieved while they were offline
    private HashMap<Integer, BlockingQueue<String>> _usernamesAwaitingMsgs;

    // A data structure to hold all usernames and passwords
    private HashMap<String,String> _usernamePassword;

    // A data structure to hold all usernames following
    private HashMap<Integer, List<Integer>> _followings;

    // A data structure to hold all usernames followers
    private HashMap<Integer, List<Integer>> _followers;


    private AtomicInteger _newestId;

    public ServerDB()
    {
        _usernamesAwaitingMsgs = new HashMap<>();
        _usernamesIds = new HashMap<>();
        _usernamePassword = new HashMap<>();
        _followings = new HashMap<>();
        _followers = new HashMap<>();
        _newestId = new AtomicInteger(0);

    }

    public int getId(String username)
    {
        Integer id = _usernamesIds.get(username);
        if (id != null)
            return id;
        return -1;
    }

    public void sendOfflineMsg(int usernameId, String usernameToSend, String msg)
    {
        if (_usernamesIds.get(usernameToSend) == usernameId)
            if (!_usernamesAwaitingMsgs.get(usernameId).contains(msg))
                _usernamesAwaitingMsgs.get(usernameId).add(msg);
    }

    public void sendOfflineMsgWithID (int usernameId, int usernameToSend, String msg)
    {

    }

    public String getAwaitingMsg(int usernameId)
    {
        BlockingQueue queue = _usernamesAwaitingMsgs.get(usernameId);
        if (queue != null)
            return (String)queue.poll();
        return null;
    }

    public int register(String username, String password)
    {
        if (_usernamePassword.containsKey(username))
            return -1;
        _usernamePassword.put(username,password);
        int lastKnown = _newestId.get();
        while (!_newestId.compareAndSet(lastKnown,lastKnown+1))
            lastKnown = _newestId.get();
        _usernamesIds.put(username,lastKnown+1);
        _followings.put(lastKnown+1,new ArrayList<>());
        _followers.put(lastKnown+1,new ArrayList<>());
        return lastKnown+1;
    }

    public int login(String username, String password)
    {
        if (_usernamePassword.containsKey(username)){
            if (_usernamePassword.get(username).equals(password))
                return _usernamesIds.get(username);
            return -2;
        }
        return -1;
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

    public List<Integer> getFollowers (int user)
    {
        return _followers.get(user);
    }


}
