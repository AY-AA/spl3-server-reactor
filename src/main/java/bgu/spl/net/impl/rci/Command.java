package bgu.spl.net.impl.rci;

import java.io.Serializable;

public interface Command<T> extends Serializable {

    String execute(T arg);
}
