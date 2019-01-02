package bgu.spl.net.api.bidi;

import bgu.spl.net.api.MessageEncoderDecoder;
import java.nio.ByteBuffer;

public class bidiMessageEncoderDecoder implements MessageEncoderDecoder<bidiMessage> {

    private bidiMessage _result = null;
    private bidiCommands _message = null;
    private final ByteBuffer _opcode = ByteBuffer.allocate(2);

    @Override
    public bidiMessage decodeNextByte(byte nextByte) {
        if (_message == null) { //indicates that we are still reading the opcode
            _result = null;
            _opcode.put(nextByte);
            if (!_opcode.hasRemaining()) { //we read 2 bytes and therefore can take the command type
                short commandIndex = bytesToShort(_opcode.array());
                parseCommand(commandIndex);
                _opcode.clear();
            }
        }
        else {
            String res =_message.decodeNextByte(nextByte);
            if (res != null) {
                _result = new bidiMessage(_message.getOpcode(),res);
                _message = null;
            }
        }
        return _result;
    }

    @Override
    public byte[] encode(bidiMessage res) {
        String cmdAndMsg = res.getString();

        int indexOfSpace = cmdAndMsg.indexOf(" ");
        String cmdString = null;
        if (indexOfSpace != -1) {
            cmdString = cmdAndMsg.substring(0, indexOfSpace);
        }
        else
            cmdString = cmdAndMsg;
        parseCommand(Integer.parseInt(cmdString));

        String msg = cmdAndMsg.substring(cmdString.length());

        byte[] ans = _message.encode(msg.trim());
        _message = null;
        return ans;
    }

    /**
     * Converts bytes into short
     * @param byteArr
     * @return
     */
    public short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }

    /**
     * finds the type of command with a given int index in OpcodeCommand enum
     * @param currCommandInt is an integer which is an index in the enum objects array
     */
    private void parseCommand(int currCommandInt) {
        OpcodeCommand opcodeCommand = OpcodeCommand.values()[currCommandInt];
        switch (opcodeCommand){
            case REGISTER:      { _message = new bidiCommands.RegisterLogin((short)1); break;}
            case LOGIN:         { _message = new bidiCommands.RegisterLogin((short)2); break;}
            case LOGOUT:        { _result =  new bidiMessage(OpcodeCommand.LOGOUT,"LOGOUT");
                                  break;                                                     }
            case FOLLOW:        { _message = new bidiCommands.Follow();                break;}
            case POST:          { _message = new bidiCommands.Post();                  break;}
            case PM:            { _message = new bidiCommands.PM();                    break;}
            case USERLIST:      { _message = new bidiCommands.Userlist();              break;}
            case STAT:          { _message = new bidiCommands.Stat();                  break;}
            case NOTIFICATION:  { _message = new bidiCommands.Notification();          break;}
            case ACK:           { _message = new bidiCommands.ACK();                   break;}
            case ERROR:         { _message = new bidiCommands.Error();                 break;}
        }
    }

}


