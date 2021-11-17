package cs451.broadcast;

import cs451.Host;
import cs451.entity.Message;
import cs451.utility.OutputWriter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

// Uniform Agreement: For any message m, if any process delivers m, then every correct process delivers m
public class UniformReliableBroadcast {
//    private final Thread send;
    private Broadcast beb;
    private final Thread deliverCheck;
    private final Thread receive;
    private ArrayList<Integer> correctProcess = new ArrayList<>();
    private LinkedBlockingQueue<Message> pending = new LinkedBlockingQueue<>();
    private HashSet<String> delivered = new HashSet<>();
    private ConcurrentHashMap<String, HashSet<Integer>> ackSet = new ConcurrentHashMap<>(100000);
    private boolean running;

    public UniformReliableBroadcast(int hostId, String hostAddr, int hostPort, List<Host> dstHosts) {
        this.beb = new Broadcast(hostId, hostAddr, hostPort, dstHosts);
        for (Host h: dstHosts){
            this.correctProcess.add(h.getId());
        }
        this.receive = new Thread( () -> {
            while(running) {
                try {
                    Message m = this.beb.receive();
                    onReceive(m.getForwardId(), m);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        this.deliverCheck = new Thread( () -> {
            while (running) {
                try {
                    Message m = this.pending.take();
                    if (canDeliver(m)) {
                        delivered.add(m.getUid());
                        urbDeliver(m);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void urbBroadcast(Message m) {
        // this.pending.add(m);
        this.beb.broadcast(m);
        OutputWriter.addLineToOutputBuffer("b "+m.getData());
    }

    public void urbDeliver(Message m) {
        OutputWriter.addLineToOutputBuffer("d "+m.getSrcId()+" "+m.getData());
    }

    public void onReceive(int forward, Message m) {
        // ack[m] := ack[m] ∪ {p};
        if (!ackSet.containsKey(m.getUid())) {
            HashSet<Integer> pList = new HashSet<>();
            pList.add(forward);
            ackSet.put(m.getUid(), pList);
            this.pending.add(m);
            this.beb.broadcast(m);
        } else {
            ackSet.get(m.getUid()).add(forward);
        }
        //if (s, m) ̸∈ pending then
        //pending := pending ∪ {(s, m)};
        //trigger ⟨ beb, Broadcast | [DATA, s, m] ⟩;
    }

    private boolean canDeliver(Message m) {
        for (Integer i: this.correctProcess) {
            HashSet<Integer> pList = this.ackSet.get(m.getUid());
            if (!pList.contains(i)){
                return false;
            }
        }
        return true;
    }

    public void start() {
        this.beb.start();
        this.running = true;
        this.receive.start();
        this.deliverCheck.start();
    }

    public void stop() {
        this.beb.stop();
        running = false;
        this.deliverCheck.interrupt();
        this.receive.interrupt();
    }
}
