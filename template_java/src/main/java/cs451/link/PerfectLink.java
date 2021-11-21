package cs451.link;

import cs451.Host;
import cs451.broadcast.observer.Observer;
import cs451.entity.Message;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.LinkedBlockingQueue;

/* PERFECT LINKS:
1) (Validity) If pi and pj are correct, then every message sent by pi to pj is eventually delivered by pj.
2) (No duplication) No message is delivered (to a process) more than once.
3) (No creation) No message is delivered unless it was sent.
*/

public class PerfectLink {
    private final UdpLink udp;

    // AckSet works for sender, it stores ack received from the receiver. It is a hashset of msg seq.
    private final ConcurrentHashMap<String, Integer> ackSet = new ConcurrentHashMap<>(100000);
    public LinkedBlockingQueue<Message> sendBuff = new LinkedBlockingQueue<>();
    public LinkedBlockingQueue<Message> deliverBuff = new LinkedBlockingQueue<>();

    // Stores array of received msg index for each process
    private final HashSet<String> delivered = new HashSet<>(100000);

    private final Thread send;
    private final Thread receive;
    private boolean running = false;

    public PerfectLink(int hostId, String hostAddr, int hostPort, List<Host> dstHosts) {
        this.udp = new UdpLink(hostId, hostAddr, hostPort, dstHosts);

        send = new Thread( () -> {
            while(running) {
                try {
                    Message m = this.sendBuff.take();
                    if (m.getType() == 0) {
                        // Send ack
                        this.udp.send(m);
                    } else {
                        // For point to point link
                        if (!this.ackSet.containsKey(m.getMsgid())) {
//                            long now = System.currentTimeMillis();
//                             // wait at least 50 to resend
//                            if (now - m.getLastSentTimestamp() < 50) {
//                                Thread.sleep(50);
//                                System.out.println("sleeping here");
//                            }
//                            m.setLastSentTimestamp(System.currentTimeMillis());
                            this.udp.send(m);
                            this.sendBuff.add(m);
                        } else {
                            this.ackSet.remove(m.getMsgid());
                        }
                    }
                } catch (InterruptedException e) {
                    // e.printStackTrace();
                }
            }
        }, "send");

        receive = new Thread( () -> {
            while(running) {
                Message m = this.udp.receive();
                if (m == null) {
                    continue;
                }
                if (m.getType() == 0 ){
                    if (!this.ackSet.containsKey(m.getMsgid())){
                        this.ackSet.put(m.getMsgid(), 1);
                    }
                } else {
                    Message ackMsg = new Message(0, m.getSrcId(), m.getSeq(), 0);
                    ackMsg.setDestination(m.getForwardHost(), m.getForwardPort());
                    this.sendBuff.add(ackMsg);
//                    m.setType(0);
//                    m.setDestination(m.getForwardHost(), m.getForwardPort());
//                    this.sendBuff.add(m);
                    if (!this.delivered.contains(m.getMsgid())) {
                        // trigger deliver event
                        this.delivered.add(m.getMsgid());
                        this.deliverBuff.add(m);
                    }
                }
            }
        }, "receive");
    }

    public void startThread() {
        running = true;
        send.start();
        receive.start();
    }


    public void stop() {
        running = false;
        this.send.interrupt();
        this.receive.interrupt();
    }

    public void addToSendBuff(ArrayList<Message> msgsToSend) {
        this.sendBuff.addAll(msgsToSend);
    }

    public void addToSendBuff(Message msgsToSend) {
        this.sendBuff.add(msgsToSend);
    }
}
