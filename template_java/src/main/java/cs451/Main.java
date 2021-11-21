package cs451;

import cs451.broadcast.FIFOBroadcast;
import cs451.broadcast.UniformReliableBroadcast;
import cs451.broadcast.observer.Observer;
import cs451.entity.Message;
import cs451.utility.SeqGenerator;
import cs451.utility.OutputWriter;

import java.util.ArrayList;

public class Main {
    private static String hostAddr;
    private static int hostPort;

    private static FIFOBroadcast bc;
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

        System.out.println("Doing some initialization\n");
        int m = parser.m();
        ArrayList<Message> msgToSend = new ArrayList<>();
        bc = new FIFOBroadcast(id, hostAddr, hostPort, dstHosts, new Observer() {
            @Override
            public void onReceive(Message m) {
                OutputWriter.addLineToOutputBuffer("d "+m.getSrcId()+" "+m.getPayload());
            }
        });
        bc.start();
        for (int j = 1; j < m+1; j++) {
            int seq = SeqGenerator.generateSeqNum();
            Message msg = new Message(2, id, seq, seq);
            msgToSend.add(msg);
            bc.broadcast(msg);
            OutputWriter.addLineToOutputBuffer("b "+msg.getPayload());
        }

        System.out.println("Broadcasting and delivering messages...\n");

        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
