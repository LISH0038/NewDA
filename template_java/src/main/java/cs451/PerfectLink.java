package cs451;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

/* PERFECT LINKS:
1) (Validity) If pi and pj are correct, then every message sent by pi to pj is eventually delivered by pj.
2) (No duplication) No message is delivered (to a process) more than once.
3) (No creation) No message is delivered unless it was sent.
*/

public class PerfectLink {
    private FairlossLink fl;
    private int[] ackSet;
    private ArrayList<int[]> receivedMsgSet;
//    private final Runnable send;
//    private final Runnable resend;
//    private final Runnable receive;
    private int hostId;
    private String dstHost;
    private int dstPort;
    private int numOfMsg;

    private final Thread send;
    private final Thread resend;
    private final Thread receive;
    private static boolean running;
    private int sentMark = 0;

    // Max seq number stored for each process: sentMark + 2048
    // private int[] sentMark;

    public PerfectLink(String hostAddr, int hostId, int hostPort, String dstAddr, int dstPort, int numOfMsg, ArrayList<Message> msgsToSend) {
        this.fl = new FairlossLink(hostAddr, hostPort);
        this.ackSet = new int[numOfMsg]; //TODO memory optimization
        this.receivedMsgSet = new ArrayList<>();
        this.hostId = hostId;
        this.dstHost = dstAddr;
        this.dstPort = dstPort;
        this.numOfMsg = numOfMsg;

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
            while(running) {
                try {
                    TimeUnit.SECONDS.sleep(5);
                    resendMsgWithNoAck();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("pl resend exit here");
        }, "resend");

        receive = new Thread( () -> {
            while(running) {
                try {
                    Message m = this.fl.getReceiveBuff().take();
                    // TODO check race condition
                    while (m.getSource() > this.receivedMsgSet.size()) {
                        this.receivedMsgSet.add(new int[this.numOfMsg]);
                    }
                    //String msgId = getMsgUniqueId(m);
                    if (m.isMsg() && receivedMsgSet.get(m.getSource()-1)[m.getSeq()-1] == 0) {
                        receivedMsgSet.get(m.getSource()-1)[m.getSeq()-1] = 1;
                        OutputWriter.addLineToOutputBuffer("d "+m.getSource()+" "+m.getSeq());
                        Message newMsg = new Message(false, this.hostId, m.getSeq());
                        newMsg.setDestination(m.getSrcHost(), m.getSrcPort());
                        this.fl.getSendBuff().put(newMsg);
                    } else if (!m.isMsg() && ackSet[m.getSeq()-1] == 0){
                        ackSet[m.getSeq()-1] = 1;
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
        boolean flag = false;
        for (int i = this.sentMark; i < this.ackSet.length; i++) {
            if (this.ackSet[i] == 0) {
                if (!flag) {
                    this.sentMark = i;
                    System.out.println("sentMark: " +i);
                    flag = true;
                }
                Message newMsg = new Message(true, this.hostId, i+1);
                newMsg.setDestination(this.dstHost, this.dstPort);
                try {
                    this.fl.getSendBuff().put(newMsg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
//
//    private String getMsgUniqueId(Message m) {
//        return m.getSource() + "a" + m.getSeq();
//    }
}
