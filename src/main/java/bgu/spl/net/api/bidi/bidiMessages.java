package bgu.spl.net.api.bidi;

import bgu.spl.net.api.MessageEncoderDecoder;
import org.omg.CORBA.IMP_LIMIT;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public abstract class bidiMessages implements MessageEncoderDecoder<String> {

    protected final String ENCODING = "utf-8";
    protected final byte _delimiter = '\0';
    protected final String _strDelimeter = "\0";
    protected final byte[] _bytesDelimiter = _strDelimeter.getBytes();

    protected int _numOfDelimiters;
    protected Vector<Byte> _byteVector = new Vector<>();

    protected byte[] _bytes;
    protected String _string = "";

    protected boolean _firstDelimiter = true;


    public abstract OpcodeCommand getOpcode();

    protected void addBytesToVector(byte[] bytes)
    {
        for (int i =0 ; i<bytes.length; i++)
        {
            _byteVector.add(bytes[i]);
        }
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

    public short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }

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

    protected void addBytes(byte[] bytesToAdd, Vector<Byte> fromVector){
        if (bytesToAdd == null)
            bytesToAdd = new byte[fromVector.size()];
        for (int i = 0; i < bytesToAdd.length; i++) {
            bytesToAdd[i] = fromVector.get(i);
        }

    }




    public static class RegisterLogin extends bidiMessages {

        private short _opcode;

        private String _username;
        private String _password;

        public RegisterLogin(short opcode) {
            _opcode = opcode;
            _numOfDelimiters = 2;
        }

        @Override
        public byte[] encode(String message) {
            message = message.trim();
            _username = message.substring(0, message.indexOf(" "));
            _password = message.substring(message.indexOf(" ") + 1);

            _string = _username + _strDelimeter + _password + _strDelimeter;

            addBytesToVector(shortToBytes(_opcode));
            addBytesToVector(_string.getBytes());

            _bytes = new byte[_byteVector.size()];
            addBytes(_bytes,_byteVector);
            return _bytes;
        }

        @Override
        public String decodeNextByte(byte nextByte) {
            if (nextByte == _delimiter){
                String str = bytesToString();
                if (_numOfDelimiters == 2) {
                    _username = str;
                    _string += str;
                }
                else {
                    _password = str;
                    _string += " " + _password;
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

    public static class Logout extends bidiMessages {

        private final String LOGOUT = "LOGOUT";
        private final short _opcode = 3;
        public Logout() {
            byte[] tmpBytes = LOGOUT.getBytes();
            addBytesToVector(tmpBytes);
            _string = bytesToString();
        }

        @Override
        public String decodeNextByte(byte nextByte) {
            return "LOGOUT";
        }

        @Override
        public byte[] encode(String message) {
            return shortToBytes(_opcode);
        }

        @Override
        public OpcodeCommand getOpcode() {
            return OpcodeCommand.LOGOUT;
        }
    }

    public static class Follow extends bidiMessages {

        private boolean _foundNumOfUsers, _followUnfollowFound = false;
        private int _counter = 0; // counts number of times reached here to know whether numOfUsers has been reached
        private final ByteBuffer _numOfUsers = ByteBuffer.allocate(2);
        private List<String> _usersList = new ArrayList<>();
        private final short _opcode = 4;

        Follow()
        {

        }

        @Override
        public String decodeNextByte(byte nextByte) {
            _counter ++;
            if (!_followUnfollowFound)
                updateFollowUnfollow(nextByte);
            else if (!_foundNumOfUsers && _counter > 1) {
                findNumOfUsers(nextByte);
                return null;
            }
            else if (nextByte == _delimiter){
                String str = bytesToString();
                _string += " " + str;
                _usersList.add(str);
            }
            return super.decodeNextByte(nextByte);
        }

        private void updateFollowUnfollow(byte nextByte) {
            _followUnfollowFound = true;
            _byteVector.add(nextByte);
            _string += nextByte;
            _byteVector.clear();
        }

        private void findNumOfUsers(byte nextByte) {
            _numOfUsers.put(nextByte);
            if (!_numOfUsers.hasRemaining())
            {
                short numOfUsers = bytesToShort(_numOfUsers.array());
                _numOfDelimiters = numOfUsers;
                _foundNumOfUsers = true;
                _string += " " + numOfUsers;
                _byteVector.clear();
            }
        }

        @Override
        public byte[] encode(String message) {
            _byteVector.clear();
            message = message.trim();

            addBytesToVector(shortToBytes(_opcode));

            String followUnfollow = message.substring(0,message.indexOf(" "));
            if (followUnfollow.contains("1"))
                _byteVector.add((byte)1);
            else
                _byteVector.add((byte)0);

            message = message.substring(message.indexOf(" ") + 1);
            String numOfUsers = message.substring(0,message.indexOf(" "));
            short numOfUsersShort = Short.parseShort(numOfUsers);
            addBytesToVector(shortToBytes(numOfUsersShort));

            message = message.substring(numOfUsers.length());
            message = message.trim();

            for (int i = 0 ; i < numOfUsersShort - 1 ; i++){
                String currString = message.substring(0,message.indexOf(" ")) + _strDelimeter;
                byte[] CurrBytes = currString.getBytes();
                addBytesToVector (CurrBytes);
                message = message.substring(message.indexOf(" ") + 1);
            }
            message = message + _strDelimeter;
            addBytesToVector(message.getBytes());


            _bytes = new byte[_byteVector.size()];
            addBytes(_bytes,_byteVector);
            return _bytes;
        }

        @Override
        public OpcodeCommand getOpcode() {
            return OpcodeCommand.FOLLOW;
        }
    }

    public static class Post extends bidiMessages {

        private final short _opcode = 5;
        Post()
        {
            _numOfDelimiters = 1;
        }

        @Override
        public String decodeNextByte(byte nextByte) {
            if (nextByte == _delimiter){
                String str = bytesToString();
                if (_firstDelimiter ){
                    _string += str;
                    _firstDelimiter = false;
                }
                else
                    _string += " " + str;
            }
            return super.decodeNextByte(nextByte);
        }

        @Override
        public byte[] encode(String message) {
            _byteVector.clear();
            message = message.trim();
            _string = message;
            addBytesToVector(shortToBytes(_opcode));
            addBytesToVector((message + _strDelimeter).getBytes());

            _bytes = new byte[_byteVector.size()];
            addBytes(_bytes,_byteVector);
            return _bytes;
        }

        @Override
        public OpcodeCommand getOpcode() {
            return OpcodeCommand.POST;
        }
    }

    public static class PM extends bidiMessages {

        private final short _opcode = 6;

        PM()
        {
            _numOfDelimiters = 2;
        }

        @Override
        public String decodeNextByte(byte nextByte) {
            if (nextByte == _delimiter){
                String str = bytesToString();
                if (!_firstDelimiter)
                    _string += " " + str;
                else{
                    _string += str;
                    _firstDelimiter = false;
                }
            }
            return super.decodeNextByte(nextByte);
        }

        @Override
        public byte[] encode(String message) {
            _byteVector.clear();
            message = message.trim();

            addBytesToVector(shortToBytes(_opcode));

            String receiver = message.substring(0,message.indexOf(" "));
            addBytesToVector((receiver + _strDelimeter).getBytes());

            String content = message.substring(message.indexOf(" ") +1);
            addBytesToVector((content + _strDelimeter).getBytes());

            _bytes = new byte[_byteVector.size()];
            addBytes(_bytes,_byteVector);
            return _bytes;
        }

        @Override
        public OpcodeCommand getOpcode() {
            return OpcodeCommand.PM;
        }
    }

    public static class Userlist extends bidiMessages {
        private final short _opcode = 7;

        Userlist(){
        }

        @Override
        public String decodeNextByte(byte nextByte) {
            return "USERLIST";
        }

        @Override
        public byte[] encode(String message) {
            return shortToBytes(_opcode);
        }

        @Override
        public OpcodeCommand getOpcode() {
            return OpcodeCommand.USERLIST;
        }

    }

    public static class Stat extends bidiMessages {

        private final short _opcode = 8;
        public Stat() {
            _numOfDelimiters =1 ;
        }

        @Override
        public String decodeNextByte(byte nextByte) {
            if (nextByte == _delimiter){
                String str = bytesToString();
                _string += str;
            }
            return super.decodeNextByte(nextByte);
        }

        @Override
        public byte[] encode(String message) {
            _byteVector.clear();
            message = message.trim();

            addBytesToVector(shortToBytes(_opcode));
            addBytesToVector((message + _strDelimeter).getBytes());

            _bytes = new byte[_byteVector.size()];
            addBytes(_bytes,_byteVector);
            return _bytes;
        }

        @Override
        public OpcodeCommand getOpcode() {
            return OpcodeCommand.STAT;
        }
    }

    public static class Notification extends bidiMessages {

        private boolean _pmPublicFound = false;
        private final short _opcode = 9;

        public Notification() {
            _numOfDelimiters = 2;
        }

        @Override
        public String decodeNextByte(byte nextByte) {
            if (!_pmPublicFound) {
                updatePmPublic(nextByte);
                return null;
            }
            else if (nextByte == _delimiter){
                String str = bytesToString();
                _string += " " + str;
                _byteVector.clear();
            }
            return super.decodeNextByte(nextByte);
        }

        private void updatePmPublic(byte nextByte) {
            _pmPublicFound = true;
            if (nextByte == 0)
                _string += "PM";
            else
                _string += "POST";
            _byteVector.clear();
        }

        @Override
        public byte[] encode(String message) {
            _byteVector.clear();
            message = message.trim();

            addBytesToVector(shortToBytes(_opcode));

            String pmPublic = message.substring(0,message.indexOf(" "));
            if (pmPublic.equals("PM"))
                _byteVector.add((byte)0);
            else
                _byteVector.add((byte)1);

            message = message.substring(message.indexOf(" ") + 1);
            String username = message.substring(0,message.indexOf(" ")) + _strDelimeter;
            addBytesToVector((username).getBytes());

            String content = message.substring(message.indexOf(" ") + 1) + _strDelimeter;
            addBytesToVector(content.getBytes());

            _bytes = new byte[_byteVector.size()];
            addBytes(_bytes,_byteVector);
            return _bytes;
        }

        @Override
        public OpcodeCommand getOpcode() {
            return OpcodeCommand.NOTIFICATION;
        }
    }

    public static class ACK extends bidiMessages {
        private boolean _noOpcode = false;  // true = ack of follow | userlist | stat
        private int _caseType = 0;              // 4 - follow, userlist | 8 - stat
        private final ByteBuffer _twoBytes = ByteBuffer.allocate(2);
        private final short _opcode = 10;

        public ACK()
        {
            byte[] tmpBytes = "ACK".getBytes();
            addBytesToVector(tmpBytes);
            _string = bytesToString();
        }

        @Override
        public String decodeNextByte(byte nextByte) {
            if (!_noOpcode && nextByte == _delimiter)
                return super.decodeNextByte(nextByte);
            else if (_caseType == 0)
                findOpcode(nextByte);
            else
                decodeByOpcodeCase(nextByte);
            return super.decodeNextByte(nextByte);
        }

        private void decodeByOpcodeCase(byte nextByte) {
            switch (_caseType){
                case 4:{
                    if (_twoBytes.hasRemaining()) {
                        _twoBytes.put(nextByte);
                        if (!_twoBytes.hasRemaining()) {
                            short numOfUsers = bytesToShort(_twoBytes.array());
                            _byteVector.clear();
                            _string += " " + numOfUsers;
                            _numOfDelimiters = numOfUsers;
                        }
                    }
                    if (nextByte == _delimiter)
                    {
                        String str = bytesToString();
                        _string += " " + str;
                    }
                    break;
                }
                case 7:{
                    _caseType = 4;
                    decodeNextByte(nextByte);
                    break;
                }
                case 8:{
                    _numOfDelimiters = 1;
                    if (nextByte == _delimiter)
                        break;
                    if (_twoBytes.hasRemaining()) {
                        _twoBytes.put(nextByte);
                        if (!_twoBytes.hasRemaining()) {
                            short currNum = bytesToShort(_twoBytes.array());
                            _byteVector.clear();
                            _string += " " + currNum;
                            _twoBytes.clear();
                        }
                    }
                    break;
                }
            }
        }

        private void findOpcode(byte nextByte) {
            _twoBytes.put(nextByte);
            if (!_twoBytes.hasRemaining())
            {
                _noOpcode = true;
                short opcodeShort = bytesToShort(_twoBytes.array());
                String opcode = OpcodeCommand.values()[opcodeShort].toString();
                _byteVector.clear();
                addBytesToVector(opcode.getBytes());
                String str = bytesToString();
                _string += " " + str;
                _caseType = opcodeShort;
                _twoBytes.clear();
            }
        }

        @Override
        public byte[] encode(String message) {
            _byteVector.clear();
            message = message.trim();

            addBytesToVector(shortToBytes(_opcode));
            if (!message.isEmpty()) {
                short opcode = Short.parseShort(message.substring(0, message.indexOf(" ")));
                _caseType = opcode;
                addBytesToVector(shortToBytes(opcode));

                encodeByCase(message);
            }
            _bytes = new byte[_byteVector.size()];
            addBytes(_bytes,_byteVector);
            return _bytes;

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
                        String currUsername = message.substring(0,message.indexOf(" ")) + _strDelimeter;
                        addBytesToVector(currUsername.getBytes());
                    }
                    String lastUsername = message.substring(message.indexOf(" ") + 1) + _strDelimeter;
                    addBytesToVector(lastUsername.getBytes());
                    break;
                }
                case 7: {
                    _caseType = 4;
                    encode(message);
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
                    addBytesToVector("'\0'".getBytes());
                    break;
                }
                default:{
                    addBytesToVector("'\0'".getBytes());
                    break;
                }
            }
        }

        @Override
        public OpcodeCommand getOpcode() {
            return OpcodeCommand.ACK;
        }
    }

    public static class Error extends bidiMessages {
        private final ByteBuffer _errorOpcode = ByteBuffer.allocate(2);
        private final short _opcode = 11;
        public Error()
        {
            _numOfDelimiters = 1;
        }
        @Override
        public String decodeNextByte(byte nextByte) {
            _errorOpcode.put(nextByte);
            if (!_errorOpcode.hasRemaining())
            {
                short numOfUsers = bytesToShort(_errorOpcode.array());
                String opcode = OpcodeCommand.values()[numOfUsers].toString();
                _byteVector.clear();
                addBytesToVector(opcode.getBytes());
                String str = bytesToString();
                _string += str;
            }
            if (_string.isEmpty())
                return null;
            return _string;
        }

        @Override
        public byte[] encode(String message) {
            message = message.trim();
            int i = 0;
            short msgOpcode = 0;
            for (OpcodeCommand opcodeCommand : OpcodeCommand.values()) {
                if (opcodeCommand.toString().equals(message))
                    msgOpcode = (short)i;
                i ++ ;
            }
            _byteVector.clear();
            addBytesToVector(shortToBytes(_opcode));
            addBytesToVector(shortToBytes(msgOpcode));
            byte[] ans = new byte[_byteVector.size()];
            addBytes(ans,_byteVector);
            return ans;
        }

        @Override
        public OpcodeCommand getOpcode() {
            return OpcodeCommand.ERROR;
        }
    }



    public static class bidiMessage
    {
        private bidiMessages _message;
        private OpcodeCommand _cmdType = OpcodeCommand.NULL;
        private List<String> _info;
        private String _encodeOrDecode, _msgToSend;

        public bidiMessage(bidiMessages message, String result){
            _message = message;
            _cmdType = _message.getOpcode();
            _msgToSend = _encodeOrDecode = result;
            parseResult();
        }

        public bidiMessage(String toEncode)
        {
            _encodeOrDecode = toEncode;
        }

        private void parseResult() {
            // init info list
            _info = new ArrayList<>();
            switch (_cmdType.toString()) {
                case "REGISTER":        { parseRegisterLogin(); break;}
                case "LOGIN":           { parseRegisterLogin(); break;}
                case "FOLLOW":          { parseFollow();        break;}
                case "POST":            { parsePost();          break;}
                case "PM":              { parsePM();            break;}
                case "STAT":            { parseStat();          break;}
                case "NOTIFICATION":    { parseNotification();  break;}
                case "ACK":             { parseACK();           break;}
                case "ERROR":           { parseERROR();         break;}
            }
        }

        private void parseRegisterLogin() {
            _encodeOrDecode.trim();
            String username = _encodeOrDecode.substring(0,_encodeOrDecode.indexOf(" "));
            String pw  = _encodeOrDecode.substring(_encodeOrDecode.indexOf(" ") +1);
            _info.add(username);
            _info.add(pw);
        }

        private void parseFollow() {
            _encodeOrDecode.trim();
            String followUnfollow = _encodeOrDecode.substring(0,_encodeOrDecode.indexOf(" "));
            _info.add(followUnfollow);

            String tmp = _encodeOrDecode.substring(_encodeOrDecode.indexOf(" ") +1);
            String numOfUsers = tmp.substring(0,_encodeOrDecode.indexOf(" "));
            _info.add(numOfUsers);

            int numOfUsersInt = Integer.parseInt(numOfUsers);
            for (int i = 0; i < numOfUsersInt - 1; i++) {
                tmp = tmp.substring(_encodeOrDecode.indexOf(" ") +1);
                String currUser = tmp.substring(0,_encodeOrDecode.indexOf(" "));
                _info.add(currUser);
            }

            String lastUser = tmp.substring(_encodeOrDecode.indexOf(" ") +1);
            _info.add(lastUser);
        }

        private void parsePost() {
            _encodeOrDecode.trim();
            String tmp = _encodeOrDecode;
            _info.add(_encodeOrDecode);
            _msgToSend = _encodeOrDecode;
            while (tmp.contains("@"))
            {
                tmp = tmp.substring(tmp.indexOf("@") + 1);
                int nextSpace = tmp.indexOf(" ");
                if (nextSpace != -1) {
                    _info.add(tmp.substring(0, nextSpace));
                }
                else {
                    _info.add(tmp);
                }
            }
        }

        private void parsePM() {
            _encodeOrDecode = _encodeOrDecode.trim();
            String username = _encodeOrDecode.substring(0, _encodeOrDecode.indexOf(" "));
            _info.add(username);

            String msg = _encodeOrDecode.substring(_encodeOrDecode.indexOf(" ") + 1);
            _msgToSend = username + " " + msg;
            _info.add(msg);
        }

        private void parseStat() {
            _encodeOrDecode.trim();
            _info.add(_encodeOrDecode);
        }

        private void parseNotification() {
            _encodeOrDecode = _encodeOrDecode.trim();
            String pmPublic = _encodeOrDecode.substring(0,_encodeOrDecode.indexOf(" "));
            _info.add(pmPublic);

            String tmp = _encodeOrDecode.substring(_encodeOrDecode.indexOf(" ") +1);
            String username = tmp.substring(0,tmp.indexOf(" "));
            _info.add(username);

            String content = tmp.substring(tmp.indexOf(" ") +1);
            _msgToSend = content;
            _info.add(content);
        }

        private void parseACK() {
            _encodeOrDecode.trim();
            _msgToSend = _encodeOrDecode;
            _info.add(_encodeOrDecode.substring(0,1));

            // in case it is follow, userlist or stat ACK
            int opcode = Integer.parseInt(_encodeOrDecode.substring(0,1));
            String currString = _encodeOrDecode;
            if (opcode == 4 || opcode == 7)
            {
                currString = currString.substring(_encodeOrDecode.indexOf(" ") + 1);
                String numOfUsers = currString.substring(0 , currString.indexOf(" "));
                _info.add(numOfUsers);
                int numOfUsersInt = Integer.parseInt(numOfUsers);

                for (int i = 0; i < numOfUsersInt - 1; i++) {
                    currString = currString.substring(_encodeOrDecode.indexOf(" ") + 1);
                    String username = currString.substring(0 , currString.indexOf(" "));
                    _info.add(username);
                }
                String lastUsername = currString.substring(_encodeOrDecode.indexOf(" ") + 1);
                _info.add(lastUsername);
            }
            else if (opcode == 8)
            {
                for (int i =0 ; i< 3 ; i++){
                    currString = currString.substring(_encodeOrDecode.indexOf(" ") + 1);
                    if (i != 2)
                        _info.add(currString.substring(0, currString.indexOf(" ")));
                    else
                        _info.add(currString);
                }
            }

        }

        private void parseERROR() {
            _encodeOrDecode.trim();
            _info.add(_encodeOrDecode);
            _msgToSend = _encodeOrDecode;
        }

        public List<String> getRelevantInfo()
        {
            return _info;
        }

        public String getString(){
            return _encodeOrDecode;
        }

        public String getCmdType()
        {
            return _cmdType.toString();
        }

        public OpcodeCommand getOpcode(){
            return _cmdType;
        }

        public String getMsgToSend()
        {
            return _msgToSend;
        }
    }
}
