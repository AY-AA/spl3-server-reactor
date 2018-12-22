package bgu.spl.net.api.bidi;

import bgu.spl.net.api.MessageEncoderDecoder;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Vector;

public class bidiMessageEncoderDecoder implements MessageEncoderDecoder<T> {

    private final String ENCODING = "utf-8";


    private OpcodeCommand _operation = null;
    private final ByteBuffer _opcode = ByteBuffer.allocate(2);
    private Vector<Byte> _byteVector = null;
    private int _numOfDelimiters;
    private final byte _delimiter = '\0';
    private boolean _followCmd = false; // if it finds the num of users, we'll know the num of delimiters
    private final ByteBuffer _followNumOfUsers = ByteBuffer.allocate(2);
    private int _followCmdFoundSizeCounter = 0;


    // --------------------- DECODE SECTION --------------------- //

    @Override
    public T decodeNextByte(byte nextByte) {
        if (_operation == null) { //indicates that we are still reading the opcode
            _opcode.put(nextByte);
            if (!_opcode.hasRemaining()) { //we read 2 bytes and therefore can take the type
                parseCommandAndNumOfDelimeters();
                _opcode.clear();
                _byteVector = new Vector<>();
                return _operation.toString();
            }
            if (_operation == OpcodeCommand.NULL)
            {
                System.out.println("an error occurred while reading a message: command type = null");
                return null;
            }
        }
        else if (_operation != OpcodeCommand.NULL){
            if (nextByte == _delimiter) {
                _numOfDelimiters--;
                if (_numOfDelimiters == 0) {
                    cleanAll();
                    return "'\0'";
                }
                String result = createCommandMsg();
                return result;
            }
            if (_followCmd){
                parseFollowCmdDelimiter(nextByte);
            }
            _byteVector.add(nextByte);
        }
        return null;
    }

    private void parseFollowCmdDelimiter(byte nextByte) {
        _followCmdFoundSizeCounter ++;
        if (_followCmdFoundSizeCounter > 1)
        {
            _followNumOfUsers.put(nextByte);
            if (!_followNumOfUsers.hasRemaining())
            {
                short commandIndex = bytesToShort(_opcode.array());
                _numOfDelimiters = commandIndex;
                _followNumOfUsers.clear();
                _followCmd = false;
                _followCmdFoundSizeCounter = 0;
            }
        }
    }

    private void parseCommandAndNumOfDelimeters() {
        short commandIndex = bytesToShort(_opcode.array());
        _operation = OpcodeCommand.values()[commandIndex];
        parseNumOfDelimiters(commandIndex);
    }

    private void parseNumOfDelimiters(short index) {
        _numOfDelimiters = -1;
        String twoDelimiters= "1,2,6";      //Register,Login,PM
        String oneDelimiter = "3,5,7,8";    //Logout,Post,Userlist,Stat
        if (oneDelimiter.contains(""+index))
            _numOfDelimiters = 1;
        else if  (twoDelimiters.contains(""+index))
            _numOfDelimiters = 2;
        else if (index == 4) {
            _followCmd = true;
        }
    }

    public short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }

    private String createCommandMsg() {
        String result = null;
        byte[] tmpArray = new byte[_byteVector.size()];
        int index = 0;
        for (Byte currByte : _byteVector){
            tmpArray[index] = currByte.byteValue();
            index++;
        }
        _byteVector.clear();
        try {
            result = new String (tmpArray,0,tmpArray.length,ENCODING);
        } catch (UnsupportedEncodingException e) {
            System.out.println("UnsupportedEncodingException : cannot create result of type String");
        }
        return result;
    }

    private void cleanAll() {
        OpcodeCommand _operation = null;
        Vector<Byte> _byteVector = null;
        _numOfDelimiters = 0;
        _followCmd = false; // if it finds the num of users, we'll know the num of delimiters
        _followCmdFoundSizeCounter = 0;
    }



    // --------------------- ENCODE SECTION --------------------- //

    @Override
    public byte[] encode(T message) {
        Vector<Byte> tmpVector = new Vector<>();
        byte[] result = null;
        short opcode = commandStringToShort(message);

        if (opcode != -1) {
            Byte[] opcodeBytes = shortToBytes(opcode);
            tmpVector.addAll(Arrays.asList(opcodeBytes));           //adding opcode
            tmpVector.addAll(createCommandBytes(opcode,message));   //adding message

            result = new byte[tmpVector.size()];
            int i = 0;
            for (Byte currByte : tmpVector) {
                result[i] = currByte.byteValue();
                i++;
            }
        }
        return result;
    }

    private short commandStringToShort(String message) {
        int cmdTypeEndingIndex = message.indexOf(" ");
        String commandType = message;
        if (cmdTypeEndingIndex != -1)
            commandType = message.substring(0,cmdTypeEndingIndex);

        int enumIndex = -1;
        for (int i = 0; i < OpcodeCommand.values().length; i++) {
            if (OpcodeCommand.values()[i].equals(commandType)) {
                enumIndex = i;
                break;
            }
        }
        return (short)enumIndex;

    }

    public Byte[] shortToBytes(short num)
    {
        Byte[] bytesArr = new Byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }

    private Vector<Byte> createCommandBytes(short opcode, String message) {
        Vector<Byte> result = new Vector<>();
        switch (opcode) {
            case 1:
                return registerLoginPMBytes(message);
            case 2:
                return registerLoginPMBytes(message);
            case 4:
                return followBytes(message);
            case 5:
                return postBytes(message);
            case 6:
                return registerLoginPMBytes(message);
            case 8:
                return statBytes(message);
            case 9:
                return notificationBytes(message);
            case 10:
                return ackErrorBytes(message,false);
            case 11:
                return ackErrorBytes(message,true);
            default:
                return result;    // 3 and 7 has no messages
        }
    }

    private Vector<Byte> registerLoginPMBytes(String message) {
        message = message.substring(message.indexOf(" ") + 1);
        Vector<Byte> result = new Vector<>();

        String username = message.substring(0,message.indexOf(" ")) + _delimiter;
        byte[] usernameBytes = username.getBytes();
        addBytes (usernameBytes,result);

        String passwordOrContent = message.substring(message.indexOf(" ") + 1) + _delimiter;
        byte[] passwordOrContentBytes = passwordOrContent.getBytes();
        addBytes (passwordOrContentBytes,result);

        return result;
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
