package bgu.spl.net.api.bidi;


import java.util.ArrayList;
import java.util.List;

/**
 * A message which is being sent by client and server to each other
 */
public class bidiMessage
{
    private OpcodeCommand _cmdType = OpcodeCommand.NULL;
    private List<String> _info;
    private String _content;

    public bidiMessage(OpcodeCommand opcode, String result){
        _cmdType = opcode;
        _content = result.trim();
        parseResult();
    }

    public bidiMessage(String toEncode) { _content = toEncode.trim(); }

    /**
     * Calling parse method of message which its opcode is _opcode
     */
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
        }
    }

    // ---------- COMMANDS PARSERS ---------- //
    private void parseRegisterLogin() { addTwoFirstDetails(); }

    private void parseFollow() {

        addTwoFirstDetails();

        String tmp = _content.substring(_content.indexOf(" ") +1);
        addUsers(tmp,Integer.parseInt(tmp.substring(0,tmp.indexOf(" "))));
    }

    private void parsePost() {
        _info.add(_content);

        String tmp = _content;
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
        String sender = _content.substring(0,_content.indexOf(" "));
        _info.add(sender);

        String content = _content.substring(_content.indexOf(" ") +1);
        _info.add(content);
    }

    private void parseStat() {
        _info.add(_content);
    }

    /**
     * Retrieves the info list
     * @return List<String> of relevant information of the message
     */
    public List<String> getRelevantInfo()
    {
        return _info;
    }

    /**
     * Retrieves the string of the message
     * @return message content String
     */
    public String getString(){ return _content; }

    /**
     * Retrieves the opcode of the message
     * @return OpcodeCommand of the message
     */
    public OpcodeCommand getOpcode(){ return _cmdType; }

    /**
     * Adds users which are included in the string into the list of information
     * @param usersString String which include usernames
     * @param numOfUsers the number of users in the {@param usersString}
     */
    private void addUsers(String usersString, int numOfUsers) {
        for (int i = 0; i < numOfUsers - 1; i++) {
            usersString = usersString.substring(usersString.indexOf(" ") + 1);
            String username = usersString.substring(0 , usersString.indexOf(" "));
            _info.add(username);
        }
        String lastUsername = usersString.substring(usersString.indexOf(" ") + 1);
        _info.add(lastUsername);
    }

    /**
     * Adds two first words of the message into the information list
     */
    private void addTwoFirstDetails() {
        String firstWord = _content.substring(0,_content.indexOf(" "));
        _info.add(firstWord);

        String tmp = _content.substring(_content.indexOf(" ") +1);

        if (tmp.contains(" "))
            _info.add(tmp.substring(0,tmp.indexOf(" ")));
        else
            _info.add(tmp);
    }


}