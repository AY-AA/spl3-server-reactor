
package bgu.spl.net.srv;

import java.io.Closeable;

/**
 * The ConnectionHandler interface for Message of type T
 */
public interface ConnectionHandler<T> extends Closeable, bgu.spl.net.srv.bidi.ConnectionHandler {

}
