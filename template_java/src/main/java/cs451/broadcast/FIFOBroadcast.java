package cs451.broadcast;

import cs451.Host;

import java.util.List;

public class FIFOBroadcast extends Broadcast {
    public FIFOBroadcast(int hostId, String hostAddr, int hostPort, List<Host> dstHosts) {
        super(hostId, hostAddr, hostPort, dstHosts);
    }
}
