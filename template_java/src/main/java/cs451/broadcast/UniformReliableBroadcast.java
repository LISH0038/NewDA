package cs451.broadcast;

import cs451.Host;
import cs451.broadcast.observer.Observer;
import cs451.entity.Message;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

// Uniform Agreement: For any message m, if any process delivers m, then every correct process delivers m
public class UniformReliableBroadcast implements Observer{
    private final Broadcast beb;
    private final Observer observer;
    private final HashSet<String> delivered = new HashSet<>();
    private final HashMap<String, HashSet<Integer>> ackSet = new HashMap<>(100000);
    private final int minVote;
    private final int hostId;

    public UniformReliableBroadcast(int hostId, String hostAddr, int hostPort, List<Host> dstHosts, Observer ob) {
        this.beb = new Broadcast(hostId, hostAddr, hostPort, dstHosts, this);
        this.observer = ob;
        this.minVote = dstHosts.size()/2;
        this.hostId = hostId;
    }

    public void broadcast(Message m) {
        this.beb.broadcast(m);
        // IMPORTANT: the above only send to others, also need to send to itself!!!
//        HashSet<Integer> pList = ackSet.getOrDefault(m.getUid(), new HashSet<>());
//        pList.add(this.hostId);
//        ackSet.put(m.getUid(), pList);
    }

    public void onReceive(Message m) {
        // ack[m] := ack[m] ∪ {p};
        // System.out.println(m.getUid());
        if (!this.delivered.contains(m.getUid())) {
            HashSet<Integer> pList = ackSet.getOrDefault(m.getUid(), new HashSet<>());
            if (m.getSrcId() != this.hostId && pList.size() == 0){
                this.beb.broadcast(m);
            }
            pList.add(m.getForwardId());
            ackSet.put(m.getUid(), pList);

            // Check if deliverable
            if (pList.size() >= this.minVote) {
                delivered.add(m.getUid());
                this.observer.onReceive(m);
                this.ackSet.remove(m.getUid());
            }
        }

    }


    public void start() {
        this.beb.start();
    }

    public void stop() {
        this.beb.stop();
    }
}
