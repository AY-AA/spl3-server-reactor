package bgu.spl.net.api.bidi;

import bgu.spl.net.api.MessageEncoderDecoder;

import java.util.Vector;

public abstract class bidiMessages implements MessageEncoderDecoder<String> {



    public class Register extends bidiMessages {
        private String _username;
        private String _password;

        public Register(String _username, String _password) {
            this._username = _username;
            this._password = _password;
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
            return OpcodeCommand.REGISTER;
        }
    }

    public class Login extends bidiMessages {
        private String _username;
        private String _password;

        public Login(String _username, String _password) {
            this._username = _username;
            this._password = _password;
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
            return OpcodeCommand.LOGIN;
        }
    }

    public class Logout extends bidiMessages {

        private final String LOGOUT = "LOGOUT";
        private byte[] _logoutBytes;

        public Logout(byte[] _logoutBytes) {
            _logoutBytes = LOGOUT.getBytes();
        }

        @Override
        public String decodeNextByte(byte nextByte) {
            return LOGOUT;
        }

        @Override
        public byte[] encode(String message) {
            return _logoutBytes;
        }

        @Override
        public OpcodeCommand getOpcode() {
            return OpcodeCommand.LOGOUT;
        }
    }

    public class Follow extends bidiMessages {

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
            return OpcodeCommand.FOLLOW;
        }
    }

    public class Post extends bidiMessages {

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
            return OpcodeCommand.POST;
        }
    }

    public class PM extends bidiMessages {

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
            return OpcodeCommand.PM;
        }
    }

    public class Userlist extends bidiMessages {

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

    public class Stat extends bidiMessages {

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
            return OpcodeCommand.STAT;
        }
    }

    public class Notification extends bidiMessages {

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
            return OpcodeCommand.NOTIFICATION;
        }
    }

    public class ACK extends bidiMessages {

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

    public class Error extends bidiMessages {

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



    public abstract OpcodeCommand getOpcode();

    private static void addBytes(byte[] bytesToAdd, Vector<Byte> vectorToAdd){
        for (int i = 0; i < bytesToAdd.length; i++) {
            vectorToAdd.add(bytesToAdd[i]);
        }

    }

}
