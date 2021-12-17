package cs451;

import cs451.broadcast.FIFOBroadcast;
import cs451.broadcast.LCBroadcast;
import cs451.broadcast.UniformReliableBroadcast;
import cs451.broadcast.observer.Observer;
import cs451.entity.Message;
import cs451.entity.VCMessage;
import cs451.utility.SeqGenerator;
import cs451.utility.OutputWriter;

import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    private static String hostAddr;
    private static int hostPort;

    private static LCBroadcast bc;
    private static String outputPath;

    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");
        if (bc != null) {
            bc.stop();
        }
        //write/flush output file if necessary
        System.out.println("Writing output.");
        OutputWriter.outputToFile(outputPath);
    }

    private static void initSignalHandlers() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                handleSignal();
            }
        });
    }

    public static void main(String[] args) throws InterruptedException {
        Parser parser = new Parser(args);
        parser.parse();

        initSignalHandlers();

        // example
        long pid = ProcessHandle.current().pid();
        System.out.println("My PID: " + pid + "\n");
        System.out.println("From a new terminal type `kill -SIGINT " + pid + "` or `kill -SIGTERM " + pid + "` to stop processing packets\n");

        System.out.println("My ID: " + parser.myId() + "\n");
        System.out.println("List of resolved hosts is:");
        System.out.println("==========================");
        for (Host host: parser.hosts()) {
            System.out.println(host.getId());
            System.out.println("Human-readable IP: " + host.getIp());
            System.out.println("Human-readable Port: " + host.getPort());
            System.out.println();
        }
        int id = parser.myId();
        ArrayList<Host> dstHosts = new ArrayList<>();
        for (Host h: parser.hosts()) {
            if (h.getId() == id) {
                hostAddr = h.getIp();
                hostPort = h.getPort();
            } else {
                dstHosts.add(h);
            }
        }
        System.out.println();

        System.out.println("Path to output:");
        System.out.println("===============");
        System.out.println(parser.output() + "\n");
        outputPath = parser.output();

        System.out.println("Path to config:");
        System.out.println("===============");
        System.out.println(parser.config() + "\n");
        System.out.println(parser.m() + "\n");
        int m = parser.m();
        HashMap<Integer, ArrayList<Integer>> causalMap = parser.getCausalMap();
        for (int i: causalMap.keySet()) {
            System.out.printf("process %d's dependency: ", i);
            for (int j=0; j < causalMap.get(i).size(); j++) {
                System.out.print(causalMap.get(i).get(j) + " ");
            }
        }

        System.out.println("Doing some initialization\n");
        bc = new LCBroadcast(id, hostAddr, hostPort, causalMap, dstHosts, new Observer() {
            @Override
            public void onReceive(Message m) {
                OutputWriter.addLineToOutputBuffer("d "+m.getSrcId()+" "+m.getPayload());
            }
        });
        bc.start();
        for (int j = 1; j < m+1; j++) {
            int seq = SeqGenerator.generateSeqNum();
            VCMessage msg = new VCMessage(1, id, seq, seq);
            OutputWriter.addLineToOutputBuffer("b "+msg.getPayload());
            bc.broadcast(msg);
            // control sending speed
            if (j%1000 == 0 ) {
                Thread.sleep(1000);
                System.out.println(bc.getSelfPendingSize());
                while (bc.getSelfPendingSize() > 1000) {
                    Thread.sleep(1000);
                    System.out.println("sending sleep");
                }
            }
        }

        System.out.println("Broadcasting finished...\n");

        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
