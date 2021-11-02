package cs451.broadcast;

import cs451.Host;

import java.util.List;

// Uniform Agreement: For any message m, if any process delivers m, then every correct process delivers m
public class UniformReliableBroadcast extends Broadcast{
//    private final Thread send;
//    private final Thread receive;

    public UniformReliableBroadcast(int hostId, String hostAddr, int hostPort, List<Host> dstHosts) {
        super(hostId, hostAddr, hostPort, dstHosts);
    }

}
