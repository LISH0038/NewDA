package cs451.broadcast.observer;

import cs451.entity.Message;

public interface Observer {
    void onReceive(Message m);
}
