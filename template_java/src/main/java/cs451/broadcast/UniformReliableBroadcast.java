package cs451.broadcast;

import cs451.Host;
import cs451.broadcast.observer.Observer;
import cs451.entity.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

// Uniform Agreement: For any message m, if any process delivers m, then every correct process delivers m
public class UniformReliableBroadcast implements Observer{
//    private final Thread send;
    private final Broadcast beb;
    private final Observer observer;
//    private final Thread deliverCheck;
//    private final Thread receive;
//    private ArrayList<Integer> correctProcess = new ArrayList<>();
//    private LinkedBlockingQueue<Message> pending = new LinkedBlockingQueue<>();
    private HashSet<String> delivered = new HashSet<>();
    private HashMap<String, HashSet<Integer>> ackSet = new HashMap<>(100000);
//    private boolean running;
    private int resendCount;
    private final int numOfProcess;

    public UniformReliableBroadcast(int hostId, String hostAddr, int hostPort, List<Host> dstHosts, Observer ob) {
        this.beb = new Broadcast(hostId, hostAddr, hostPort, dstHosts, this);
        this.observer = ob;
//        for (Host h: dstHosts){
//            this.correctProcess.add(h.getId());
//        }
        this.numOfProcess = dstHosts.size() + 1;

//        this.deliverCheck = new Thread( () -> {
//            while (running) {
//                try {
//                    Message m = this.pending.take();
//                    if (canDeliver(m)) {
//                        delivered.add(m.getUid());
//                        urbDeliver(m);
//                    } else {
//                        this.pending.add(m);
//                    }
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }

    public void broadcast(Message m) {
        // this.pending.add(m);
        this.beb.broadcast(m);
//        OutputWriter.addLineToOutputBuffer("b "+m.getPayload());
    }

    public void onReceive(Message m) {
        // ack[m] := ack[m] ∪ {p};
        // System.out.println(m.getUid());
        if (!this.delivered.contains(m.getUid())) {
            if (!ackSet.containsKey(m.getUid())) {
                HashSet<Integer> pList = new HashSet<>();
                pList.add(m.getForwardId());
                ackSet.put(m.getUid(), pList);
//            this.pending.add(m);
                this.beb.broadcast(m);
                resendCount ++;
                if (resendCount% 10000== 2000){
                    // System.out.println("urb forward count: " + resendCount + " " +System.currentTimeMillis()/1000);
                }
            } else {
                HashSet<Integer> pList = ackSet.get(m.getUid());
                pList.add(m.getForwardId());

                // Check if deliverable
                if (pList.size() > this.numOfProcess/2) {
                    delivered.add(m.getUid());
                    this.observer.onReceive(m);
                    this.ackSet.remove(m.getUid());
                }
            }
        }

    }

//    private boolean canDeliver(Message m) {
//        for (Integer i: this.correctProcess) {
//            HashSet<Integer> pList = this.ackSet.get(m.getUid());
//            if (!pList.contains(i)){
//                return false;
//            }
//        }
//        return true;
//    }

    public void start() {
        this.beb.start();
//        this.running = true;
//        this.receive.start();
//        this.deliverCheck.start();
    }

    public void stop() {
        this.beb.stop();
//        running = false;
//        this.deliverCheck.interrupt();
//        this.receive.interrupt();
    }
}
