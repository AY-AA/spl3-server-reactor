package bgu.spl.net.impl.newsfeed;

import bgu.spl.net.api.bidi.bidiMessages;
import bgu.spl.net.impl.rci.Command;
import java.io.Serializable;

public class FetchNewsCommand implements Command<bidiMessages.bidiMessage> {

    private String channel;

    public FetchNewsCommand(String channel) {
        this.channel = channel;
    }

    @Override
    public String execute(bidiMessages.bidiMessage feed) {
        return feed.getString();
    }

}
