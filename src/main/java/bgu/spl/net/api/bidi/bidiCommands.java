package bgu.spl.net.api.bidi;

import bgu.spl.net.api.MessageEncoderDecoder;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * bidiMessages defines the class which encode and decode messages in the server
 */
public abstract class bidiCommands implements MessageEncoderDecoder<String> {

    protected final String ENCODING = "utf-8";
    protected final byte _delimiter = '\0';
    protected final String _strDelimeter = "\0";

    protected int _numOfDelimiters;
    protected Vector<Byte> _byteVector = new Vector<>();

    protected String _message = "";


    /**
     * Retrieves the opcode type of the message
     * @return
     */
    public abstract OpcodeCommand getOpcode();

    /**
     * adds bytes from a given array into _byteVector
     * @param bytes array to copy from
     */
    protected void addBytesToVector(byte[] bytes)
    {
        for (int i =0 ; i<bytes.length; i++)
            _byteVector.add(bytes[i]);
    }

    @Override
    public String decodeNextByte(byte nextByte) {
        if (nextByte == _delimiter) {
            _numOfDelimiters--;
            if (_numOfDelimiters == 0) {
                return _message;
            }
        }
        else {
            _byteVector.add(nextByte);
        }
        return null;
    }

    @Override
    public byte[] encode(String message) {
        return null;
    }

    /**
     * encodes a given short into 2 bytes
     * @param num value in range 1 to 11
     * @return an array of bytes
     */
    public byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }

    /**
     * decodes a given array into a short
     * @param byteArr is an array 2-size representing a number in range 1 to 11
     * @return short represented by {@param byteArr}
     */
    public short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }

    /**
     * decodes _byteVector into a String
     * @return a string decoded of _byteVector
     */
    protected String bytesToString() {
        byte[] bytes = new byte[_byteVector.size()];
        int i = 0;
        for (Byte currByte : _byteVector) {
            bytes[i] = currByte.byteValue();
            i++;
        }
        _byteVector.clear();
        String str = null;
        try {
            str = new String(bytes, 0, bytes.length , ENCODING);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * adds bytes from _byteVector into a given array
     * @param bytesToAdd an array to add bytes
     */
    protected void addBytes(byte[] bytesToAdd){
        if (bytesToAdd == null)
            bytesToAdd = new byte[_byteVector.size()];
        for (int i = 0; i < bytesToAdd.length; i++) {
            bytesToAdd[i] = _byteVector.get(i);
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        bidiCommands messages = (bidiCommands) o;
        return Objects.equals(_message, messages._message);
    }

    /**
     * A class represents the Register or Login message delivered from the client
     */
    public static class RegisterLogin extends bidiCommands {

        private short _opcode;

        public RegisterLogin(short opcode) {
            _opcode = opcode;
            _numOfDelimiters = 2;
        }

        @Override
        public String decodeNextByte(byte nextByte) {
            if (nextByte == _delimiter){
                String str = bytesToString();
                if (_numOfDelimiters == 2) {
                    _message += str;
                }
                else {
                    _message += " " + str;
                }
            }
            return super.decodeNextByte(nextByte);
        }

        @Override
        public OpcodeCommand getOpcode() {
            if (_opcode == 1)
                return OpcodeCommand.REGISTER;
            else
                return OpcodeCommand.LOGIN;
        }

    }
    /**
     * A class represents the Logout message delivered from the client
     */
    public static class Logout extends bidiCommands {

        public Logout() { }

        @Override
        public String decodeNextByte(byte nextByte) { return "LOGOUT"; }

        @Override
        public OpcodeCommand getOpcode() { return OpcodeCommand.LOGOUT; }
    }
    /**
     * A class represents the Follow message delivered from the client
     */
    public static class Follow extends bidiCommands {

        private boolean _foundNumOfUsers, _followUnfollowFound = false;
        private int _counter = 1; // counts number of times reached here to know whether numOfUsers has been reached
        private final ByteBuffer _numOfUsers = ByteBuffer.allocate(2);
        private List<String> _usersList = new ArrayList<>();

        Follow() { }

        @Override
        public String decodeNextByte(byte nextByte) {
            _counter ++;
            if (!_followUnfollowFound)
                updateFollowUnfollow(nextByte);
            else if (!_foundNumOfUsers) {
                findNumOfUsers(nextByte);
                return null;
            }
            else if (nextByte == _delimiter){
                String str = bytesToString();
                _message += " " + str;
                _usersList.add(str);
            }
            return super.decodeNextByte(nextByte);
        }

        private void updateFollowUnfollow(byte nextByte) {
            if (nextByte == '1' || nextByte == (byte)1)
                _message += " 1";
            else
                _message += " 0";
            _followUnfollowFound = true;
            _byteVector.clear();
        }

        private void findNumOfUsers(byte nextByte) {
            _numOfUsers.put(nextByte);
            if (!_numOfUsers.hasRemaining())
            {
                short numOfUsers = bytesToShort(_numOfUsers.array());
                _numOfDelimiters = numOfUsers;
                _foundNumOfUsers = true;
                _message += " " + numOfUsers;
                _byteVector.clear();
            }
        }

        @Override
        public OpcodeCommand getOpcode() { return OpcodeCommand.FOLLOW; }
    }
    /**
     * A class represents the Post message delivered from the client
     */
    public static class Post extends bidiCommands {
        private boolean _firstDelimiter = true;

        Post(){ _numOfDelimiters = 1; }

        @Override
        public String decodeNextByte(byte nextByte) {
            if (nextByte == _delimiter){
                String str = bytesToString();
                if (_firstDelimiter ){
                    _message += str;
                    _firstDelimiter = false;
                }
                else
                    _message += " " + str;
            }
            return super.decodeNextByte(nextByte);
        }

        @Override
        public OpcodeCommand getOpcode() { return OpcodeCommand.POST; }
    }
    /**
     * A class represents the PM message delivered from the client
     */
    public static class PM extends bidiCommands {
        private boolean _firstDelimiter = true;

        PM() { _numOfDelimiters = 2; }

        @Override
        public String decodeNextByte(byte nextByte) {
            if (nextByte == _delimiter){
                String str = bytesToString();
                if (!_firstDelimiter)
                    _message += " " + str;
                else{
                    _message += str;
                    _firstDelimiter = false;
                }
            }
            return super.decodeNextByte(nextByte);
        }

        @Override
        public OpcodeCommand getOpcode() { return OpcodeCommand.PM; }
    }
    /**
     * A class represents the Userlist message delivered from the client
     */
    public static class Userlist extends bidiCommands {

        Userlist(){
        }

        @Override
        public String decodeNextByte(byte nextByte) { return "USERLIST"; }

        @Override
        public OpcodeCommand getOpcode() { return OpcodeCommand.USERLIST;}

    }
    /**
     * A class represents the Stat message delivered from the client
     */
    public static class Stat extends bidiCommands {

        public Stat() { _numOfDelimiters =1 ; }

        @Override
        public String decodeNextByte(byte nextByte) {
            if (nextByte == _delimiter){
                String str = bytesToString();
                _message += str;
            }
            return super.decodeNextByte(nextByte);
        }

        @Override
        public OpcodeCommand getOpcode() { return OpcodeCommand.STAT; }
    }
    /**
     * A class represents the Notification message sent by the server
     */
    public static class Notification extends bidiCommands {

        private final short _opcode = 9;

        public Notification() { _numOfDelimiters = 2; }

        @Override
        public byte[] encode(String message) {
            _byteVector.clear();
            message = message.trim();

            addBytesToVector(shortToBytes(_opcode));

            String pmPublic = message.substring(0,message.indexOf(" "));
            if (pmPublic.equals("0"))
                _byteVector.add((byte)0);
            else
                _byteVector.add((byte)1);

            message = message.substring(message.indexOf(" ") + 1);
            String username = message.substring(0,message.indexOf(" ")) + _strDelimeter;
            addBytesToVector((username).getBytes());

            String content = message.substring(message.indexOf(" ") + 1) + _strDelimeter;
            addBytesToVector(content.getBytes());

            byte[] resultBytes  = new byte[_byteVector.size()];
            addBytes(resultBytes);
            return resultBytes ;
        }

        @Override
        public OpcodeCommand getOpcode() { return OpcodeCommand.NOTIFICATION; }
    }
    /**
     * A class represents the ACK message sent by the server
     */
    public static class ACK extends bidiCommands{
        private int _caseType = 0;              // 4 - follow, userlist | 8 - stat
        private final short _opcode = 10;

        public ACK() {        }

        @Override
        public byte[] encode(String message) {
            _byteVector.clear();
            message = message.trim();

            addBytesToVector(shortToBytes(_opcode));

            short opcode;

            if (message.contains(" "))
                opcode = Short.parseShort(message.substring(0, message.indexOf(" ")));
            else
                opcode = Short.parseShort(message);

            _caseType = opcode;
            addBytesToVector(shortToBytes(opcode));

            encodeByCase(message);

            byte[] resultBytes = new byte[_byteVector.size()];
            addBytes(resultBytes);
            return resultBytes;
        }

        private void encodeByCase(String message) {
            switch(_caseType)
            {
                case 4: {
                    message = message.substring(message.indexOf(" ") + 1);
                    short numOfUsers = Short.parseShort(message.substring(0,message.indexOf(" ")));
                    addBytesToVector(shortToBytes(numOfUsers));
                    for (int i = 0; i < numOfUsers -1 ; i++) {
                        message = message.substring(message.indexOf(" ") + 1);
                        String currUsername = message.substring(0,message.indexOf(" "));
                        addBytesToVector(currUsername.getBytes());
                        _byteVector.add(_delimiter);
                    }
                    String lastUsername = message.substring(message.indexOf(" ") + 1);
                    addBytesToVector(lastUsername.getBytes());
                    _byteVector.add(_delimiter);
                    break;
                }
                case 7: {
                    _caseType = 4;
                    encodeByCase(message);
                    break;
                }
                case 8: {
                    for (int i = 0; i < 3; i++) {
                        message = message.substring(message.indexOf(" ") + 1);
                        short currShort;
                        if (i == 2)
                            currShort = Short.parseShort(message);
                        else
                            currShort = Short.parseShort(message.substring(0,message.indexOf(" ")));
                        addBytesToVector(shortToBytes(currShort));
                    }
                    _byteVector.add(_delimiter);
                    break;
                }
                default:{
                    _byteVector.add(_delimiter);
                    break;
                }
            }
        }

        @Override
        public OpcodeCommand getOpcode() { return OpcodeCommand.ACK; }
    }
    /**
     * A class represents theError message sent by the server
     */
    public static class Error extends bidiCommands {

        private final short _opcode = 11;

        public Error(){ }

        @Override
        public byte[] encode(String message) {
            message = message.trim();
            short msgOpcode = Short.parseShort(message);
            _byteVector.clear();
            addBytesToVector(shortToBytes(_opcode));
            addBytesToVector(shortToBytes(msgOpcode));
            _byteVector.add(_delimiter);
            byte[] ans = new byte[_byteVector.size()];
            addBytes(ans);
            return ans;
        }

        @Override
        public OpcodeCommand getOpcode() { return OpcodeCommand.ERROR; }
    }


}
