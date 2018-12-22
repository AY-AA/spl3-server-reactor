package bgu.spl.net.api.bidi;


/**
 * Protocol is responsible for Syntax, Semantics and Synchronization
 * @param <T>
 */
public interface BidiMessagingProtocol<T>  {
    /**
     * Used to initiate the current client protocol with it's personal connection ID and the connections implementation
     **/
    void start(int connectionId, Connections<T> connections);

    /**
     * Processes a given message, responses sent via 'send' method of Connections object.
     * @param message
     */
    void process(T message);

    /**
     * @return true if the connection should be terminated
     */
    boolean shouldTerminate();
}