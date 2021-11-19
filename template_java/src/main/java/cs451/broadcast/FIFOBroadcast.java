package cs451.broadcast;

import cs451.Host;
import cs451.broadcast.observer.Observer;
import cs451.entity.Message;
import cs451.utility.OutputWriter;

import java.util.List;

public class FIFOBroadcast implements Observer {
    private final UniformReliableBroadcast urb;

    public FIFOBroadcast(int hostId, String hostAddr, int hostPort, List<Host> dstHosts) {
        this.urb = new UniformReliableBroadcast(hostId, hostAddr, hostPort, dstHosts, this);
    }
    public void broadcast(Message m) {
        // this.pending.add(m);
        this.urb.broadcast(m);
    }

    public void onReceive(Message m) {
//        OutputWriter.addLineToOutputBuffer("d "+m.getSrcId()+" "+m.getPayload());
    }

}
