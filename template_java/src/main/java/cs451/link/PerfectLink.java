package cs451.link;

import cs451.utility.OutputWriter;
import cs451.entity.Message;

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

    // AckSet works for sender, it stores ack received from the receiver. It is a hashset of msg seq.
    private ConcurrentHashMap<Integer, Integer> ackSet;
    private LinkedBlockingQueue<Message> sendBuff = new LinkedBlockingQueue<>();

    // Stores array of received msg index for each process
    private ArrayList<HashSet<Integer>> receivedMsgIdSet = new ArrayList<>();;

    private int hostId;
    private final Thread send;
    private final Thread receive;
    private boolean running = false;
    private boolean isSender;

    public PerfectLink(String hostAddr, int hostId, int hostPort, boolean isSender) {
        this.udp = new UdpLink(hostAddr, hostPort);
        if (isSender) {
            this.ackSet = new ConcurrentHashMap<>(100000);
        }

        this.hostId = hostId;
        this.isSender = isSender;

        send = new Thread( () -> {
//            int count  = 0;
            while(running) {
                try {
                    Message m = this.sendBuff.take();
                    if (m.isMsg()) {
                        if (!this.ackSet.containsKey(m.getId())) {
                            if (!m.isSent()) {
                                OutputWriter.addLineToOutputBuffer("b "+m.getData());
                                m.setSent(true);
                            }
                            this.udp.send(m);
                            this.sendBuff.add(m);
                            //System.out.println("sending "+m.getId());
                        } else {
                            this.ackSet.remove(m.getId());
                        }
                    } else {
                        this.udp.send(m);
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
//                count += 1;
                if (this.isSender) {
                    if (!m.isMsg() && !ackSet.containsKey(m.getId())){
                        ackSet.put(m.getId(), 1);
                    }
                    //System.out.println(Thread.currentThread().getName() + " msg: " + m);
                } else {
                    while (m.getSource() > this.receivedMsgIdSet.size()) {
                        this.receivedMsgIdSet.add(new HashSet<>(100000));
                    }
                    if (m.isMsg()) {
                        Message newMsg = new Message(false, this.hostId, m.getId(), 0);
                        newMsg.setDestination(m.getSrcHost(), m.getSrcPort());
                        this.sendBuff.add(newMsg);

                        if (!receivedMsgIdSet.get(m.getSource()-1).contains(m.getId())) {
                            receivedMsgIdSet.get(m.getSource()-1).add(m.getId());
                            OutputWriter.addLineToOutputBuffer("d "+m.getSource()+" "+m.getData());
                        }
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
}
