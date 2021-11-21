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

public class Broadcast {
    private final PerfectLink pl;
    private final Observer observer;
    private final List<Host> dstHosts;

    private final Thread receive;
    private boolean running = false;

    public Broadcast(int hostId, String hostAddr, int hostPort, List<Host> dstHosts, Observer ob) {
        this.pl = new PerfectLink(hostId, hostAddr, hostPort, dstHosts);
        this.observer = ob;
        this.dstHosts = dstHosts;
        this.receive = new Thread( () -> {
            while (running) {
                try {
                    Message m = this.pl.deliverBuff.take();
                    this.observer.onReceive(m);
                } catch (InterruptedException e) {
//                    e.printStackTrace();
                }
            }
        });
    }

    public void broadcast(Message m) {
        for (Host h: dstHosts) {
            Message mCopy = new Message(1, m.getSrcId(), m.getSeq(), m.getPayload());
            mCopy.setForwardId(h.getId()); // expecting dst id
//            mCopy.setDstId(h.getId());
            mCopy.setDestination(h.getIp(), h.getPort());
            pl.addToSendBuff(mCopy);
        }
//        System.out.println(System.currentTimeMillis()/1000+" broadcast "+m.getMsgid());
    }

    public void start() {
        running = true;
        this.receive.start();
        pl.startThread();
    }

    public void stop() {

        running = false;
        this.receive.interrupt();
        pl.stop();
    }
}
