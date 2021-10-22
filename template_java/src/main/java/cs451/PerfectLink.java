package cs451;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/* PERFECT LINKS:
1) (Validity) If pi and pj are correct, then every message sent by pi to pj is eventually delivered by pj.
2) (No duplication) No message is delivered (to a process) more than once.
3) (No creation) No message is delivered unless it was sent.
*/

public class PerfectLink {
    private FairlossLink fl;
    // AckSet works for sender, it stores ack received from the receiver. It is a hashset of msg seq.
    private HashSet<Integer> ackSet;
    private ConcurrentLinkedQueue<Message> resendBuff = new ConcurrentLinkedQueue<>();
//private int ackMark = 0;

    private ArrayList<HashSet<Integer>> receivedMsgIndexSet;
//    private final Runnable send;
//    private final Runnable resend;
//    private final Runnable receive;
    private int hostId;

    private final Thread resend;
    private final Thread receive;
    private boolean running;
    private boolean isSender;

    // Max seq number stored for each process: sentMark + 2048
    // private int[] sentMark;

    public PerfectLink(String hostAddr, int hostId, int hostPort, boolean isSender) {
        this.fl = new FairlossLink(hostAddr, hostPort);
        if (isSender) {
            this.ackSet = new HashSet<>(100000); //TODO memory optimization
        } else {
            this.receivedMsgIndexSet = new ArrayList<>();
        }
        this.hostId = hostId;
        this.isSender = isSender;

        resend = new Thread( () -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while(running) {
                try {
                    Thread.sleep(1000);
                    resendMsgWithNoAck();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("pl resend exit here");
        }, "resend");

        receive = new Thread( () -> {
            int count = 0;
            while(running) {
                if (this.isSender) {
                    try {
                        Message m = this.fl.getReceiveBuff().take();
                        count += 1;
                        if (!m.isMsg() && !ackSet.contains(m.getSeq())){
                            ackSet.add(m.getSeq());
                        }
                        //System.out.println(Thread.currentThread().getName() + " msg: " + m);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Message m = this.fl.getReceiveBuff().take();
                        count += 1;
                        // TODO check race condition
                        while (m.getSource() > this.receivedMsgIndexSet.size()) {
                            this.receivedMsgIndexSet.add(new HashSet<>(100000));
                        }
                        //String msgId = getMsgUniqueId(m);
                        if (m.isMsg()) {
                            if (!receivedMsgIndexSet.get(m.getSource()-1).contains(m.getSeq())) {
                                receivedMsgIndexSet.get(m.getSource()-1).add(m.getSeq());
                                //TODO new thread for output
                                OutputWriter.addLineToOutputBuffer("d "+m.getSource()+" "+m.getSeq());
                            }
                            Message newMsg = new Message(false, this.hostId, m.getSeq());
                            newMsg.setDestination(m.getSrcHost(), m.getSrcPort());
                            this.fl.getSendBuff().put(newMsg);
                        }
                        //System.out.println(Thread.currentThread().getName() + " msg: " + m);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(System.currentTimeMillis()%1000 == 0) {
                    System.out.println("receive count: "+count);
                }
            }
        }, "receive");
    }

    public void startThread() {
        running = true;
        if (this.isSender) {
            //this.fl.startSend();
            this.fl.startReceive();
            //Thread t1 = new Thread(this.send, "send");
            //Thread t2 = new Thread(this.receive, "receive");
            resend.start();
            //Thread t3 = new Thread(this.resend, "resend");
            receive.start();
        } else {
            //this.fl.startSend();
            this.fl.startReceive();
            //Thread t2 = new Thread(this.receive, "receive");
            receive.start();
        }
    }


    public void stop() {
        running = false;
        this.fl.stop();
        if (this.isSender) {
            this.resend.interrupt();
        }
        this.receive.interrupt();
    }

    public void send(ArrayList<Message> msgsToSend) {
        for (Message m: msgsToSend) {
            try {
                this.fl.getSendBuff().put(m);
                this.resendBuff.add(m);
                OutputWriter.addLineToOutputBuffer("b "+m.getSeq());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("pl send exit here");
    }

    private void resendMsgWithNoAck() throws InterruptedException {
//        boolean updateAckMark = false;
//        for (int i = this.ackMark + 1; i < this.ackMark + 10001; i++) {
//            if (!this.ackSet.contains(i)) {
//                if (!updateAckMark) {
//                    this.ackMark = i - 1;
//                    System.out.println("ackMark: " +(i -1));
//                    updateAckMark = true;
//                }
//                try {
//                    this.fl.getSendBuff().put(this.msgToSend.);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        if (!updateAckMark) {
//            this.ackMark += 10000;
//        }
        int count = 0;
        ArrayList<Message> newToSend = new ArrayList<>();
        while (!this.resendBuff.isEmpty()){
            Message m = this.resendBuff.poll();
            if (!this.ackSet.contains(m.getSeq())) {
                count +=1;
                newToSend.add(m);
                this.fl.getSendBuff().put(m);
            }
        }
        this.resendBuff.addAll(newToSend);

        System.out.println("resend count: " + count);
    }
//
//    private String getMsgUniqueId(Message m) {
//        return m.getSource() + "a" + m.getSeq();
//    }
}