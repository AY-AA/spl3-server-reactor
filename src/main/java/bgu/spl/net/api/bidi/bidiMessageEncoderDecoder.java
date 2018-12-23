package bgu.spl.net.api.bidi;

import bgu.spl.net.api.MessageEncoderDecoder;
import java.nio.ByteBuffer;

public class bidiMessageEncoderDecoder implements MessageEncoderDecoder<bidiMessages.bidiMessage> {

    private bidiMessages.bidiMessage _result = null;
    private bidiMessages _message = null;
    private final ByteBuffer _opcode = ByteBuffer.allocate(2);

    // --------------------- DECODER --------------------- //

    @Override
    public bidiMessages.bidiMessage decodeNextByte(byte nextByte) {
        if (_message == null) { //indicates that we are still reading the opcode
            _opcode.put(nextByte);
            if (!_opcode.hasRemaining()) { //we read 2 bytes and therefore can take the command type
                boolean hasMoreData = parseCommand();
                _opcode.clear();
                if (!hasMoreData) {
                    cleanAll();
                    String res = _message.decodeNextByte(nextByte);
                    _result = new bidiMessages.bidiMessage(_message,res);
                }
            }
        }
        else {
            String res =_message.decodeNextByte(nextByte);
            if (res != null) {
                cleanAll();
                _result = new bidiMessages.bidiMessage(_message,res);
            }
        }
        return _result;
    }

    private boolean parseCommand() {
        short commandIndex = bytesToShort(_opcode.array());
        OpcodeCommand opcodeCommand = OpcodeCommand.values()[commandIndex];
        switch (opcodeCommand){
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
    }

    public short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }

    private void cleanAll() {
        _message = null;
    }

    // --------------------- ENCODE SECTION --------------------- //

    @Override
    public byte[] encode(bidiMessages.bidiMessage res) {
        String cmdAndMsg = res.getString();


        int indexOfSpace = cmdAndMsg.indexOf(" ");
        String cmdString = null;
        if (indexOfSpace != -1) {
            cmdString = cmdAndMsg.substring(0, indexOfSpace);
        }
        else
            cmdString = cmdAndMsg;
        parseCommand(cmdString);

        String msg = cmdAndMsg.substring(cmdString.length());

//        res = new bidiMessages.bidiMessage(_message,msg);

        byte[] ans = _message.encode(msg);
        cleanAll();
        return ans;

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

}


