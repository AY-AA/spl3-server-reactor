package bgu.spl.net.api.bidi;

import bgu.spl.net.api.MessageEncoderDecoder;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public abstract class bidiMessages implements MessageEncoderDecoder<String> {
    protected final String ENCODING = "utf-8";
    private boolean _finished = false;
    protected final byte _delimiter = '\0';

    protected int _numOfDelimiters;
    protected Vector<Byte> _byteVector = null;


    protected byte[] _bytes;
    protected String _string;


    public abstract OpcodeCommand getOpcode();

    public short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }


    private void addBytes(byte[] bytesToAdd, Vector<Byte> vectorToAdd){
        for (int i = 0; i < bytesToAdd.length; i++) {
            vectorToAdd.add(bytesToAdd[i]);
        }

    }

    protected String bytesToString() {
        byte[] bytes = new byte[_byteVector.size()];
        int i = 0;
        for (Byte currByte : _byteVector) {
            bytes[i] = currByte;
            i++;
        }
        _byteVector.clear();
        String str = null;
        try {
            str = new String(bytes, 0, bytes.length, ENCODING);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }



    @Override
    public String decodeNextByte(byte nextByte) {
        if (nextByte == _delimiter) {
            _numOfDelimiters--;
            if (_numOfDelimiters == 0) {
                return _string;
            }
        }
        else {
            _byteVector.add(nextByte);
        }
        return null;
    }


    @Override
    public byte[] encode(String message) {
        return _bytes;
    }

    public byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }

    public static class RegisterLogin extends bidiMessages {

        private short _opcode;

        private String _username;
        private String _password;

        public RegisterLogin(short opcode) {
            _opcode = opcode;
            _numOfDelimiters = 2;
            if (opcode == 1)
                _string = "LOGIN";
            else
                _string = "REGISTER";
        }

        @Override
        public byte[] encode(String message) {
            message.trim();
            _username = message.substring(0, message.indexOf(" "));
            _password = message.substring(message.indexOf(" ") + 1);

            _string += _username + '\0' + _password + '\0';

            byte[] opcode = shortToBytes(_opcode);
            byte[] string = _string.getBytes();

            _bytes = new byte[opcode.length + string.length];

            return _bytes;
        }


        @Override
        public String decodeNextByte(byte nextByte) {
            if (nextByte == _delimiter){
                String str = bytesToString();
                if (_delimiter == 2) {
                    _username = str;
                    _string = str;
                }
                else {
                    _password = str;
                    _string = _string + " " +  _username + " " + _password;
                }
            }
            return super.decodeNextByte(nextByte);
        }

        @Override
        public OpcodeCommand getOpcode() {
            return OpcodeCommand.REGISTER;
        }

    }

    public static class Logout extends bidiMessages {

        private final String LOGOUT = "LOGOUT";
        private final short _opcode = 3;
        public Logout() {
            _string = LOGOUT;
        }

        @Override
        public String decodeNextByte(byte nextByte) {
            return LOGOUT;
        }

        @Override
        public byte[] encode(String message) {
            _bytes =  shortToBytes(_opcode);
            return _bytes;
        }

        @Override
        public OpcodeCommand getOpcode() {
            return OpcodeCommand.LOGOUT;
        }
    }

    public static class Follow extends bidiMessages {

        private byte _followUnfollow;
        private boolean _foundNumOfUsers, _followUnfollowFound = false;
        private int _counter = 0; // counts number of times reached here to know whether numOfUsers has been reached
        private final ByteBuffer _numOfUsers = ByteBuffer.allocate(2);
        private List<String> _usersList = new ArrayList<>();

        Follow()
        {
            _string = "FOLLOW";
        }

        @Override
        public String decodeNextByte(byte nextByte) {
            if (!_followUnfollowFound) {
                updateFollowUnfollow(nextByte);
            }
            _counter ++;
            if (!_foundNumOfUsers && _counter > 2) {
                findNumOfUsers(nextByte);
                return null;
            }
            else if (nextByte == _delimiter){
                String str = bytesToString();
                _string = _string + " " + str;
                _usersList.add(str);
            }
            return super.decodeNextByte(nextByte);
        }

        private void updateFollowUnfollow(byte nextByte) {
            _followUnfollow = nextByte;
            _followUnfollowFound = true;
            if (nextByte == 0)
                _string += " follow";
            else
                _string += " unfollow";
            _byteVector.clear();
        }

        private void findNumOfUsers(byte nextByte) {
            _numOfUsers.put(nextByte);
            if (!_numOfUsers.hasRemaining())
            {
                short numOfUsers = bytesToShort(_numOfUsers.array());
                _numOfDelimiters = numOfUsers;
                _foundNumOfUsers = true;
                _string = _string + " " + numOfUsers;
                _byteVector.clear();
            }
        }


        @Override
        public byte[] encode(String message) {
            return null;
        }

        @Override
        public OpcodeCommand getOpcode() {
            return OpcodeCommand.FOLLOW;
        }
    }

    public static class Post extends bidiMessages {

        List<String> _usersToSend = new ArrayList<>();

        Post()
        {
            _numOfDelimiters = 1;
            _string = "POST";
        }

        @Override
        public String decodeNextByte(byte nextByte) {
            if (nextByte == _delimiter){
                String str = bytesToString();
                _string += " " + str;
                findAdditionalReceipters();
            }
            return super.decodeNextByte(nextByte);
        }

        private void findAdditionalReceipters() {
            String tmp = _string;
            while (tmp.contains("@"))
            {
                tmp = tmp.substring(tmp.indexOf("@") + 1);
                int nextSpace = tmp.indexOf(" ");
                if (nextSpace != -1)
                    _usersToSend.add(tmp.substring(0, nextSpace));
                else
                    _usersToSend.add(tmp);
            }
        }

        @Override
        public byte[] encode(String message) {
            return null;
        }

        @Override
        public OpcodeCommand getOpcode() {
            return OpcodeCommand.POST;
        }
    }

    public static class PM extends bidiMessages {

        private String _username;
        private String _content;

        PM()
        {
            _numOfDelimiters = 2;
            _string = "PM";
        }

        @Override
        public String decodeNextByte(byte nextByte) {
            if (nextByte == _delimiter){
                String str = bytesToString();
                _string += " " + str;
                if (_numOfDelimiters == 2)
                    _username = str;
                else
                    _content = str;
            }
            return super.decodeNextByte(nextByte);
        }

        @Override
        public byte[] encode(String message) {
            return null;
        }

        @Override
        public OpcodeCommand getOpcode() {
            return OpcodeCommand.PM;
        }
    }

    public static class Userlist extends bidiMessages {
        Userlist(){
            _string = "USERLIST";
        }

        @Override
        public String decodeNextByte(byte nextByte) {
            return null;
        }

        @Override
        public byte[] encode(String message) {
            return null;
        }

        @Override
        public OpcodeCommand getOpcode() {
            return OpcodeCommand.USERLIST;
        }

    }

    public static class Stat extends bidiMessages {

        private String _username;
        public Stat() {
            _string = "STAT";
            _numOfDelimiters =1 ;
        }

        @Override
        public String decodeNextByte(byte nextByte) {
            if (nextByte == _delimiter){
                String str = bytesToString();
                _string += " " + str;
                _username = str;
            }
            return super.decodeNextByte(nextByte);
        }

        @Override
        public byte[] encode(String message) {
            return null;
        }

        @Override
        public OpcodeCommand getOpcode() {
            return OpcodeCommand.STAT;
        }
    }

    public static class Notification extends bidiMessages {

        private byte _pmPublic;
        private boolean _pmPublicFound = false;
        private String _username, _content;

        public Notification() {
            _string = "NOTIFICATION";
            _numOfDelimiters = 2;
        }

        @Override
        public String decodeNextByte(byte nextByte) {
            if (!_pmPublicFound) {
                updatePmPublic(nextByte);
            }
            else if (nextByte == _delimiter){
                String str = bytesToString();
                _string = _string + " " + str;
                if (_numOfDelimiters == 2)
                    _username = str;
                else
                    _content = str;
            }
            return super.decodeNextByte(nextByte);
        }

        private void updatePmPublic(byte nextByte) {
            _pmPublic = nextByte;
            _pmPublicFound = true;
            if (nextByte == 0)
                _string += " PM";
            else
                _string += " POST";
            _byteVector.clear();
        }

        @Override
        public byte[] encode(String message) {
            return null;
        }

        @Override
        public OpcodeCommand getOpcode() {
            return OpcodeCommand.NOTIFICATION;
        }
    }


    //TODO : ACK
    public static class ACK extends bidiMessages {

        @Override
        public String decodeNextByte(byte nextByte) {
            return null;
        }

        @Override
        public byte[] encode(String message) {
            return null;
        }

        @Override
        public OpcodeCommand getOpcode() {
            return OpcodeCommand.ACK;
        }
    }
    //TODO : ERROR
    public static class Error extends bidiMessages {

        @Override
        public String decodeNextByte(byte nextByte) {
            return null;
        }

        @Override
        public byte[] encode(String message) {
            return null;
        }

        @Override
        public OpcodeCommand getOpcode() {
            return OpcodeCommand.ERROR;
        }
    }





}
