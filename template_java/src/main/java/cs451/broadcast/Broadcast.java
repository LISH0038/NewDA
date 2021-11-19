package cs451.broadcast;

import cs451.Host;
import cs451.broadcast.observer.Observer;
import cs451.entity.Message;
import cs451.link.PerfectLink;

import java.util.List;

// Best Effort Broadcast

// BEB1. Validity: If pi and pj are correct, then every message broadcast by pi is eventually delivered by pj
// BEB2. No duplication: No message is delivered more than once
// BEB3. No creation: No message is delivered unless it was broadcast

public class Broadcast implements Observer {
    private final PerfectLink pl;
    private final Observer observer;

    public Broadcast(int hostId, String hostAddr, int hostPort, List<Host> dstHosts, Observer ob) {
        this.pl = new PerfectLink(hostId, hostAddr, hostPort, dstHosts, this);
        this.observer = ob;
    }

    public void broadcast(Message m) {
        m.setType(2);
        pl.addToSendBuff(m);
    }

    public void onReceive(Message m) {
        this.observer.onReceive(m);
    }

    public void start() {
        pl.startThread();
    }

    public void stop() {
        pl.stop();
    }
}
