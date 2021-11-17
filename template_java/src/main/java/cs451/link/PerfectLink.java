package cs451.link;

import cs451.Host;
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
    private UdpLink udp;
    public List<Host> dstHosts;

    // AckSet works for sender, it stores ack received from the receiver. It is a hashset of msg seq.
    private ConcurrentHashMap<String, Integer> ackSet = new ConcurrentHashMap<>(100000);
    public LinkedBlockingQueue<Message> sendBuff = new LinkedBlockingQueue<>();
    public LinkedBlockingQueue<Message> deliverBuff = new LinkedBlockingQueue<>();

    // Stores array of received msg index for each process
    private HashSet<String> delivered = new HashSet<>(100000);

    private int hostId;
    private final Thread send;
    private final Thread receive;
    private boolean running = false;

    public PerfectLink(int hostId, String hostAddr, int hostPort, List<Host> dstHosts) {
        this.udp = new UdpLink(hostId, hostAddr, hostPort, dstHosts);
        this.hostId = hostId;
        this.dstHosts = dstHosts;

        send = new Thread( () -> {
//            int count  = 0;
            while(running) {
                try {
                    Message m = this.sendBuff.take();
                    if (m.getType() == 0) {
                        // Send ack
                        this.udp.send(m);
                    } else if (m.getType() == 2){
                        // For Broadcast
                        if (!this.ackSet.containsKey(m.getUid())) {
                            if (!m.isSent()) {
                                m.setSent(true); // ? I forgot why I put this
                            }
                            this.udp.sendToAll(m);
                            // this.sendBuff.add(m);
                            for (Host h: dstHosts) {
                                Message mCopy = new Message(1, m.getSrcId(), m.getSeq(), m.getData());
                                mCopy.setForwardId(this.hostId);
                                mCopy.setDstId(h.getId());
                                mCopy.setDestination(h.getIp(), h.getPort());
                                this.sendBuff.add(mCopy);
                            }
                            //System.out.println("sending "+m.getId());
                        } else {
                            this.ackSet.remove(m.getUid());
                        }
                    }  else if (m.getType() == 1){
                        // For point to point link
                        if (!this.ackSet.containsKey(m.getUid())) {
                            if (!m.isSent()) {
                                m.setSent(true);
                            }
                            this.udp.send(m);
                            this.sendBuff.add(m);
                            //System.out.println("sending "+m.getId());
                        } else {
                            this.ackSet.remove(m.getUid());
                        }
                    }
                    //System.out.println("send " + ++count + " " +System.currentTimeMillis());
                } catch (InterruptedException e) {
                    // e.printStackTrace();
                }
            }
        }, "send");

        receive = new Thread( () -> {
//            int count = 0;
            while(running) {
                Message m = this.udp.receive();
                if (m == null) {
                    continue;
                }
//                count += 1;
                if (m.getType() == 0 ){
                    if (!this.ackSet.containsKey(m.getUid())){
                        this.ackSet.put(m.getUid(), 1);
                    }
                    System.out.println(m.getUid());
                } else {
                    Message ackMsg = new Message(0, m.getSrcId(), m.getSeq(), 0);
                    ackMsg.setDestination(m.getForwardHost(), m.getForwardPort());
                    this.sendBuff.add(ackMsg);

                    if (!this.delivered.contains(m.getUid())) {
                        // trigger deliver event
                        this.delivered.add(m.getUid());
                        this.deliverBuff.add(m);
                    }
                    //System.out.println("receive " + ++count + " " +System.currentTimeMillis());
                }
//                if(System.currentTimeMillis()%1000 == 0) {
//                    System.out.println("receive count: "+count);
//                }
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
