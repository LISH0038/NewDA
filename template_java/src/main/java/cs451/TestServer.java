package cs451;

import java.net.*;
import java.io.IOException;

public class TestServer extends Thread {

    private DatagramSocket socket;
    private boolean running;
    private byte[] buf = new byte[256];

    public TestServer() {
        try {
            socket = new DatagramSocket(4445);
        } catch (SocketException e) {
            System.err.println("Problem with the server file!");
        }
    }

    public void run() {
        running = true;

        while (running) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                System.err.println("Problem with the server file!");
            }

            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            System.err.println("addr & port: " + address + " " + port);

            packet = new DatagramPacket(buf, buf.length, address, port);
            String received
                    = new String(packet.getData(), 0, packet.getLength());

            if (received.equals("end")) {
                running = false;
                continue;
            }

            try {socket.send(packet);}
            catch (IOException e) {
                System.err.println("Problem with the server file!");
                return;
            }

        }
        socket.close();
    }
}