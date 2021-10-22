package cs451;

import java.util.ArrayList;

public class Main {
    private static String hostAddr;
    private static int hostPort;
    private static String dstAddr;
    private static int dstPort;
    private static PerfectLink pl;
    private static String outputPath;

    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");
        pl.stop();
        //write/flush output file if necessary
        System.out.println("Writing output.");
        OutputWriter.outputToFile(outputPath);
//       System.exit(0);
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
        for (Host h: parser.hosts()) {
            if (h.getId() == id) {
                hostAddr = h.getIp();
                hostPort = h.getPort();
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
        int i = parser.i();

        if (id == i) {
            // this process serves as receiving only
            pl = new PerfectLink(hostAddr, id, hostPort, "", 0, new ArrayList<>());
            pl.startReceive();
        } else {
            // this process serves as sending only
            for (Host h: parser.hosts()) {
                if (h.getId() == i) {
                    dstAddr = h.getIp();
                    dstPort = h.getPort();
                }
            }

            ArrayList<Message> msgToSend = new ArrayList<>();
            for (int j = 1; j < m+1; j++) {
                Message msg = new Message(true, id, j);
                msg.setDestination(dstAddr, dstPort);
                msgToSend.add(msg);
            }
            pl = new PerfectLink(hostAddr, id, hostPort, dstAddr, dstPort, msgToSend);
            pl.startSend();
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
