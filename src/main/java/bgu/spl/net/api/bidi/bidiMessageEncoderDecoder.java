package bgu.spl.net.api.bidi;

import bgu.spl.net.api.MessageEncoderDecoder;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Vector;

public class bidiMessageEncoderDecoder implements MessageEncoderDecoder<String> {

    private bidiMessages _message = null;
    private final ByteBuffer _opcode = ByteBuffer.allocate(2);

    // --------------------- DECODER --------------------- //

    @Override
    public String decodeNextByte(byte nextByte) {
        if (_message == null) { //indicates that we are still reading the opcode
            _opcode.put(nextByte);
            if (!_opcode.hasRemaining()) { //we read 2 bytes and therefore can take the command type
                boolean hasMoreData = parseCommand();
                _opcode.clear();
                if (!hasMoreData) {
                    String res = _message.decodeNextByte(nextByte);
                    cleanAll();
                }
            }
        }
        else {
            String res =_message.decodeNextByte(nextByte);
            if (res != null)
                cleanAll();
            return res;
        }
        return null;
    }

    private boolean parseCommand() {
        short commandIndex = bytesToShort(_opcode.array());
        OpcodeCommand opcodeCommand = OpcodeCommand.values()[commandIndex];
        switch (opcodeCommand){
            case NULL:          { _message = null; } break;
            case REGISTER:      { _message = new bidiMessages.RegisterLogin((short)1); return true; }
            case LOGIN:         { _message = new bidiMessages.RegisterLogin((short)2); return true; }
            case LOGOUT:        { _message = new bidiMessages.Logout();                return false;}
            case FOLLOW:        { _message = new bidiMessages.Follow();                return true; }
            case POST:          { _message = new bidiMessages.Post();                  return true; }
            case PM:            { _message = new bidiMessages.PM();                    return true; }
            case USERLIST:      { _message = new bidiMessages.Userlist();              return false;}
            case STAT:          { _message = new bidiMessages.Stat();                  return true; }
            case NOTIFICATION:  { _message = new bidiMessages.Notification();          return true; }
            case ACK:           { _message = new bidiMessages.ACK();                   return true; }
            case ERROR:         { _message = new bidiMessages.Error();                 return true; }
            default:            return false;
        }
        return false;
    }

    public short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }

    private void cleanAll() {
        _message = null;
        _opcode.clear();
    }

    // --------------------- ENCODE SECTION --------------------- //

    @Override
    public byte[] encode(String str) {
        int indexOfSpace = str.indexOf(" ");
        String cmdString = null;
        if (indexOfSpace != -1) {
            cmdString = str.substring(0, indexOfSpace);
        }
        else
            cmdString = str;
        parseCommand(cmdString);
        String msg = str.substring(0, cmdString.length());

        return _message.encode(msg);
    }

    private void parseCommand(String command) {
        switch (command) {
            case "REGISTER":
            {_message = new bidiMessages.RegisterLogin((short)1);       break;}
            case "LOGIN":
            {    _message = new bidiMessages.RegisterLogin((short)2);   break;}
            case "LOGOUT":
            {    _message = new bidiMessages.Logout();                  break;}
            case "FOLLOW":
            {    _message = new bidiMessages.Follow();                  break;}
            case "POST":
            {    _message = new bidiMessages.Post();                    break;}
            case "PM":
            {   _message = new bidiMessages.PM();                       break;}
            case "USERLIST":
            {   _message = new bidiMessages.Userlist();                 break;}
            case "STAT":
            {    _message = new bidiMessages.Stat();                    break;}
            case "NOTIFICATION":
            {   _message = new bidiMessages.Notification();             break;}
            case "ACK":
            {   _message = new bidiMessages.ACK();                      break;}
            case "ERROR":
            {   _message = new bidiMessages.Error();                    break;}
        }
    }

    

    private Vector<Byte> followBytes(String message) {
        message = message.substring(message.indexOf(" ") + 1);
        Vector<Byte> result = new Vector<>();
        String followUnfollow = message.substring(0,message.indexOf(" "));
        if (followUnfollow.contains("1"))
            result.add((byte)1);
        else
            result.add((byte)0);

        message = message.substring(message.indexOf(" ") + 1);
        String numOfUsers = message.substring(0,message.indexOf(" "));
        short numOfUsersShort = Short.parseShort(numOfUsers);
        result.addAll(Arrays.asList(shortToBytes(numOfUsersShort)));

        int users = numOfUsersShort;
        String currString;
        for (int i = 0 ; i < users ; i++){
            message = message.substring(message.indexOf(" ") + 1);
            if (i != users -1)
                currString = message.substring(0,message.indexOf(" ")) + _delimiter;
            else
                currString = message + _delimiter;
            byte[] CurrBytes = currString.getBytes();
            addBytes (CurrBytes,result);
        }
        return result;
    }

    private Vector<Byte> postBytes(String message) {
        message = message.substring(message.indexOf(" ") + 1);
        Vector<Byte> result = new Vector<>();

        message = message + _delimiter;
        byte[] bytes = message.getBytes();
        addBytes(bytes,result);

        return result;
    }

    private Vector<Byte> statBytes(String message) {
        message = message.substring(message.indexOf(" ") + 1) + _delimiter;
        Vector<Byte> result = new Vector<>();

        addBytes(message.getBytes(),result);

        return result;
    }

    private Vector<Byte> notificationBytes(String message) {
        Vector<Byte> result = new Vector<>();
        message = message.substring(message.indexOf(" ") + 1);

        String pmPublic = message.substring(0,message.indexOf(" "));
        if (pmPublic.contains("0"))
            result.add((byte)0);
        else
            result.add((byte)1);

        message = message.substring(message.indexOf(" ") + 1);
        String username = message.substring(0,message.indexOf(" ")) + _delimiter;
        byte[] usernameBytes = username.getBytes();
        addBytes(usernameBytes,result);

        String content = message.substring(message.indexOf(" ") + 1) + _delimiter;
        addBytes(content.getBytes(),result);
        return result;
    }

    private Vector<Byte> ackErrorBytes(String message,boolean isError) {
        Vector<Byte> result = new Vector<>();
        message = message.substring(message.indexOf(" ") + 1);

        short opcode = commandStringToShort(message);
        if (opcode != -1) {
            Byte[] opcodeBytes = shortToBytes(opcode);
            result.addAll(Arrays.asList(opcodeBytes));
        }
        if (isError)
            return result;
        if (opcode == 4 || opcode == 7 || opcode == 8) {    //optional part addition
            message = message.substring(message.indexOf(" ") + 1);
            short numOfUsers = Short.parseShort(message.substring(0,message.indexOf(" ")));
            result.addAll(Arrays.asList(shortToBytes(numOfUsers)));
            if (opcode == 8)
                ackStatBytes(message, result);
            else
                ackFollowUserListBytes(message, result, numOfUsers);
        }
        return result;
    }

    private void ackStatBytes(String message, Vector<Byte> result) {
        message = message.substring(message.indexOf(" ") + 1);
        short numOfUsers = Short.parseShort(message.substring(0,message.indexOf(" ")));
        result.addAll(Arrays.asList(shortToBytes(numOfUsers)));
        message = message.substring(message.indexOf(" ") + 1);
        numOfUsers = Short.parseShort(message);
        result.addAll(Arrays.asList(shortToBytes(numOfUsers)));
    }

    private void ackFollowUserListBytes(String message, Vector<Byte> result, int numOfUsers) {
        for (int i =0 ; i < numOfUsers -1 ; i++)
        {
            message = message.substring(message.indexOf(" ") + 1);
            String currUser = message.substring(0,message.indexOf(" "));
            addBytes(currUser.getBytes(),result);

        }
        message = message.substring(message.indexOf(" ") + 1) + _delimiter;
        addBytes(message.getBytes(),result);
    }

    private void addBytes(byte[] bytesToAdd,Vector<Byte> vectorToAdd){
        for (int i = 0; i < bytesToAdd.length; i++) {
            vectorToAdd.add(bytesToAdd[i]);
        }

    }

}
