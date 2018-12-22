package bgu.spl.net.api.bidi;

public class bidiMessagingProtocolImpl implements BidiMessagingProtocol<bidiMessages> {

    private int ID;
    private Connections<bidiMessages> _connections;
    private boolean _shouldTerminate;

    @Override
    public void start(int connectionId, Connections<bidiMessages> connections) {
        ID = connectionId;
        _connections = connections;
    }

    @Override
    public void process(bidiMessages message) {
        OpcodeCommand opcodeCommand = message.getOpcode();
        switch (opcodeCommand){
            case NULL:                          return;
            case REGISTER:      register();     break;
            case LOGIN:         login();        break;
            case LOGOUT:        logout();       break;
            case FOLLOW:        follow();       break;
            case POST:          post();         break;
            case PM:            pm();           break;
            case USERLIST:      userlist();     break;
            case STAT:          stat();         break;
            case NOTIFICATION:  notification(); break;
            case ACK:           ack();          break;
            case ERROR:         error();        break;
            default:                            return;
        }
    }

    private void register() {

    }

    private void login() {

    }

    private void logout() {

    }

    private void follow() {

    }

    private void post() {

    }

    private void pm() {

    }

    private void userlist() {

    }

    private void stat() {

    }

    private void notification() {

    }

    private void ack() {

    }

    private void error() {

    }


    @Override
    public boolean shouldTerminate() {
        return _shouldTerminate;
    }
}
