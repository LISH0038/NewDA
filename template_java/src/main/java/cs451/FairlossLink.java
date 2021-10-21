package cs451;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ArrayBlockingQueue;

public class FairlossLink {
    private DatagramSocket socket;
    private ArrayBlockingQueue<Message> sendBuff = new ArrayBlockingQueue<>(20);
    private ArrayBlockingQueue<Message> receiveBuff = new ArrayBlockingQueue<>(20);

    private final Thread t1;
    private final Thread t2;
    private static boolean running;

    public FairlossLink(String host, int port) {
        try {
            this.socket = new DatagramSocket(port, InetAddress.getByName(host));
            running = true;
        } catch (Exception e){
            e.printStackTrace();
        }

        t1 = new Thread( () -> {
            while(running) {
                try {
                    Message msg = sendBuff.take();
                    send(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("fl send exit here");
        }, "send");

        t2 = new Thread( () -> {
            while(running) {
                try {
                    Message msg = receive();
                    if (msg != null) {
                        receiveBuff.put(msg);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("fl receive exit here");
        }, "receive");
    }

    public ArrayBlockingQueue<Message> getSendBuff() {
        return sendBuff;
    }

    public ArrayBlockingQueue<Message> getReceiveBuff() {
        return receiveBuff;
    }

    public void startSend() {
        // TODO try more threads
        t1.start();
    }

    public void startReceive() {
        // TODO try more threads
        t2.start();
    }

    private synchronized void send(Message msg)  {
        Payload data = new Payload(msg.isMsg(), msg.getSource(), msg.getSeq());
        byte[] buff = Serializer.serialize(data);
        try {
            DatagramPacket packet = new DatagramPacket(buff, buff.length, InetAddress.getByName(msg.getDstHost()), msg.getDstPort());
            socket.send(packet);
        } catch (Exception e) {
           // e.printStackTrace();
        }
    }

    private Message receive() {
        byte[] buff = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buff,buff.length);
        try {
            socket.receive(packet);
        } catch (IOException e) {
            //e.printStackTrace();
            return null;
        }
        Payload data = Serializer.deserialize(packet.getData());
        Message msg = new Message(data.isMsg(), data.getSource(), data.getSeq());
        msg.setSource(packet.getAddress().getHostAddress(), packet.getPort());
        return msg;
    }

    public void stop(){
        running = false;
        this.socket.close();
        this.t1.interrupt();
        this.t2.interrupt();
    }

}
