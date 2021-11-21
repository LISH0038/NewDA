package cs451.broadcast;

import cs451.Host;
import cs451.broadcast.observer.Observer;
import cs451.entity.Message;
import cs451.entity.SortedBuffer;

import java.util.HashMap;
import java.util.List;

public class FIFOBroadcast implements Observer {
    private final UniformReliableBroadcast urb;
    private final Observer observer;

    private final HashMap<Integer, SortedBuffer> pendingList = new HashMap<>();
    private SortedBuffer selfPendingList = new SortedBuffer();

    public FIFOBroadcast(int hostId, String hostAddr, int hostPort, List<Host> dstHosts, Observer observer) {
        this.urb = new UniformReliableBroadcast(hostId, hostAddr, hostPort, dstHosts, this);
        this.observer = observer;
        for (Host h: dstHosts) {
            this.pendingList.put(h.getId(), new SortedBuffer());
        }
        this.pendingList.put(hostId, selfPendingList);
    }

    public void broadcast(Message m) {
        this.urb.broadcast(m);
    }

    public void onReceive(Message m) {
        SortedBuffer pending = this.pendingList.get(m.getSrcId());
        if (pending.getWatermark() == m.getSeq()) {
            this.observer.onReceive(m);
            pending.incrementWatermark();

            if (pending.hasNewLowest() || pending.getLowest() == pending.getWatermark()) {
                clearBufferedMsg(pending);
            }
        } else {
            pending.getSortedBuffer().add(m);
            if (m.getSeq() < pending.getLowest()) {
                pending.setNewLowest(true);
            }
        }
//        System.out.println(pending.getWatermark()+" fifo "+pending.getLowest()+" :"+m.getUid());
    }

    private void clearBufferedMsg(SortedBuffer pending) {
        if (!pending.getSortedBuffer().isEmpty()) {
            int lowest = pending.getSortedBuffer().first().getSeq();
            while (lowest == pending.getWatermark()) {
                this.observer.onReceive(pending.getSortedBuffer().pollFirst());
                pending.incrementWatermark();
                if (!pending.getSortedBuffer().isEmpty()) {
                    lowest = pending.getSortedBuffer().first().getSeq();
                } else {
                    lowest = 2147483647;
                }
            }
            pending.setLowest(lowest);
        }

        pending.setNewLowest(false);
        // up to here, watermark is the (max delivered seq) + 1;
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
