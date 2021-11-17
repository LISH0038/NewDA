package cs451.broadcast;

import cs451.Host;
import cs451.entity.Message;
import cs451.link.PerfectLink;
import cs451.utility.OutputWriter;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

// Best Effort Broadcast

// BEB1. Validity: If pi and pj are correct, then every message broadcast by pi is eventually delivered by pj
// BEB2. No duplication: No message is delivered more than once
// BEB3. No creation: No message is delivered unless it was broadcast

public class Broadcast {
    private final PerfectLink pl;

    public Broadcast(int hostId, String hostAddr, int hostPort, List<Host> dstHosts) {
        this.pl = new PerfectLink(hostId, hostAddr, hostPort, dstHosts);
    }

    public void broadcast(Message m) {
        m.setType(2);
        pl.addToSendBuff(m);
    }

//    public void receive(Message m) {
//
//        OutputWriter.addLineToOutputBuffer("d "+m.getSource()+" "+m.getData());
//    }

    public Message receive() throws InterruptedException {
        return this.pl.deliverBuff.take();
    }

    public void start() {
        pl.startThread();
    }

    public void stop() {
        pl.stop();
    }
}
