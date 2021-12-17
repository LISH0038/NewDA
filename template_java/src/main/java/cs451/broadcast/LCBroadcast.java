package cs451.broadcast;

import cs451.Host;
import cs451.broadcast.observer.Observer;
import cs451.entity.Message;
import cs451.entity.SortedBuffer;
import cs451.entity.VCMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class LCBroadcast implements Observer {
    private final UniformReliableBroadcast urb;
    private final Observer observer;
//    private final HashMap<Integer, Integer> vc = new HashMap<>();
    private final ArrayList<Integer> selfDependency;
    private final HashMap<Integer, ArrayList<Integer>> reverseCausal;
    private final ConcurrentHashMap<Integer, SortedBuffer> pendingList = new ConcurrentHashMap<>();
    private final SortedBuffer selfPendingList = new SortedBuffer();

    public LCBroadcast(int hostId, String hostAddr, int hostPort, HashMap<Integer, ArrayList<Integer>> causalMap, List<Host> dstHosts, Observer observer) {
        this.urb = new UniformReliableBroadcast(hostId, hostAddr, hostPort, dstHosts, this);
        this.observer = observer;
        for (Host h: dstHosts) {
            this.pendingList.put(h.getId(), new SortedBuffer());
        }
        this.reverseCausal = getReverseCausalMap(causalMap);
        this.selfDependency = causalMap.get(hostId);
        this.pendingList.put(hostId, selfPendingList);
    }

    public void broadcast(VCMessage m) {
        m.setVc(getVCSnapshot());
        this.urb.broadcast(m);
    }

    public void onReceive(Message m) {
        SortedBuffer pending = this.pendingList.get(m.getSrcId());
        if (canDeliver((VCMessage) m)) {
            this.observer.onReceive(m);
            pending.incrementWatermark();

            if (pending.hasNewLowest() || pending.getLowest() == pending.getWatermark()) {
                clearBufferedMsg(pending);
                ArrayList<Integer> reverse = this.reverseCausal.get(m.getSrcId());
                if (reverse != null) {
                    for (int affected : reverse) {
                        clearBufferedMsg(this.pendingList.get(affected));
                    }
                }
            }
        } else {
            pending.getSortedBuffer().add(m);
            if (m.getSeq() < pending.getLowest()) {
                pending.setNewLowest(true);
            }
        }
//        System.out.println(pending.getWatermark()+" fifo "+pending.getLowest()+" :"+m.getUid());
    }

    private boolean canDeliver(VCMessage m) {
        // self fifo requirement
        if (this.pendingList.get(m.getSrcId()).getWatermark() != m.getSeq()) {
            return false;
        }

        // other dependencies
        if (m.getVc() != null) {
            System.out.printf("lcb receives: src=%d, seq=%d, expect=%s, current=%s\n",
                    m.getSrcId(), m.getSeq(), m.getVc().toString(), getVCSnapshotAll().toString());
            for (int dep: m.getVc().keySet()) {
                int watermark = this.pendingList.get(dep).getWatermark();
                int expected = m.getVc().get(dep);

                if (watermark < expected)
                    return false;
            }
        }

        return true;
    }

    private void clearBufferedMsg(SortedBuffer pending) {
        if (!pending.getSortedBuffer().isEmpty()) {
            VCMessage nextMsg = (VCMessage) pending.getSortedBuffer().first();
            int lowest = nextMsg.getSeq();
            while (canDeliver(nextMsg)) {
                this.observer.onReceive(pending.getSortedBuffer().pollFirst());
                pending.incrementWatermark();
                if (!pending.getSortedBuffer().isEmpty()) {
                    nextMsg = (VCMessage) pending.getSortedBuffer().first();
                    lowest = nextMsg.getSeq();
                } else {
                    lowest = 2147483647;
                    break;
                }
            }
            pending.setLowest(lowest);
        }

        pending.setNewLowest(false);
        // up to here, watermark is the (max delivered seq) + 1;
    }

    private HashMap<Integer, Integer> getVCSnapshot() {
        HashMap<Integer, Integer> vc = new HashMap<>();
        for (int i : this.selfDependency) {
            vc.put(i, this.pendingList.get(i).getWatermark());
        }
        return vc;
    }

    // for testing print only
    private HashMap<Integer, Integer> getVCSnapshotAll() {
        HashMap<Integer, Integer> vc = new HashMap<>();
        for (int i : this.pendingList.keySet()) {
            vc.put(i, this.pendingList.get(i).getWatermark());
        }
        return vc;
    }

    private static HashMap<Integer, ArrayList<Integer>> getReverseCausalMap(HashMap<Integer, ArrayList<Integer>> causalMap) {
        HashMap<Integer, ArrayList<Integer>> reverse = new HashMap<>();
        for (int i : causalMap.keySet()) {
            for(int dep: causalMap.get(i)) {
                ArrayList<Integer> r = reverse.getOrDefault(dep, new ArrayList<>());
                r.add(i);
                reverse.put(dep, r);
            }
        }
        return reverse;
    }

    public int getSelfPendingSize() {
        return this.selfPendingList.getSortedBuffer().size();
    }

    public void start() {
        this.urb.start();
    }

    public void stop() {
        this.urb.stop();
    }
}
