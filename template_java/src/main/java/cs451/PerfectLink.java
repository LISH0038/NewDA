package cs451;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

/* PERFECT LINKS:
1) (Validity) If pi and pj are correct, then every message sent by pi to pj is eventually delivered by pj.
2) (No duplication) No message is delivered (to a process) more than once.
3) (No creation) No message is delivered unless it was sent.
*/

public class PerfectLink {
    private FairlossLink fl;
    private HashSet<Integer> ackSet;
    private int ackMark = 0;

    private ArrayList<HashSet<Integer>> receivedMsgIndexSet;
//    private final Runnable send;
//    private final Runnable resend;
//    private final Runnable receive;
    private int hostId;
    private String dstHost;
    private int dstPort;

    private final Thread send;
    private final Thread resend;
    private final Thread receive;
    private static boolean running;

    // Max seq number stored for each process: sentMark + 2048
    // private int[] sentMark;

    public PerfectLink(String hostAddr, int hostId, int hostPort, String dstAddr, int dstPort, ArrayList<Message> msgsToSend) {
        this.fl = new FairlossLink(hostAddr, hostPort);
        //TODO remove numOfMsg
        this.ackSet = new HashSet<>(100000); //TODO memory optimization
        this.receivedMsgIndexSet = new ArrayList<>();
        this.hostId = hostId;
        this.dstHost = dstAddr;
        this.dstPort = dstPort;

        send = new Thread( () -> {
            for (Message m: msgsToSend) {
                try {
                    this.fl.getSendBuff().put(m);
                    OutputWriter.addLineToOutputBuffer("b "+m.getSeq());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("pl send exit here");
        }, "send");

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
                try {
                    Message m = this.fl.getReceiveBuff().take();
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
                    } else if (!m.isMsg() && !ackSet.contains(m.getSeq())){
                        ackSet.add(m.getSeq());
                    }
                    //System.out.println(Thread.currentThread().getName() + " msg: " + m);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("pl receive exit here");
        }, "receive");
    }

    public void startSend() {
        // TODO try with more threads
        running = true;
        this.fl.startSend();
        this.fl.startReceive();
        //Thread t1 = new Thread(this.send, "send");
        send.start();
        //Thread t2 = new Thread(this.receive, "receive");
        resend.start();
        //Thread t3 = new Thread(this.resend, "resend");
        receive.start();
    }

    public void startReceive() {
        // TODO try with more threads
        running = true;
        this.fl.startSend();
        this.fl.startReceive();
        //Thread t2 = new Thread(this.receive, "receive");
        receive.start();
    }

    public void stop() {
        running = false;
        this.fl.stop();
        this.send.interrupt();
        this.resend.interrupt();
        this.receive.interrupt();
    }

    private void resendMsgWithNoAck() {
        boolean updateAckMark = false;
        for (int i = this.ackMark + 1; i < this.ackMark + 10001; i++) {
            if (!this.ackSet.contains(i)) {
                if (!updateAckMark) {
                    this.ackMark = i - 1;
                    System.out.println("ackMark: " +(i -1));
                    updateAckMark = true;
                }
                Message newMsg = new Message(true, this.hostId, i);
                newMsg.setDestination(this.dstHost, this.dstPort);
                try {
                    this.fl.getSendBuff().put(newMsg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (!updateAckMark) {
            this.ackMark += 10000;
        }
        System.out.println("exit from resend function");
    }
//
//    private String getMsgUniqueId(Message m) {
//        return m.getSource() + "a" + m.getSeq();
//    }
}
