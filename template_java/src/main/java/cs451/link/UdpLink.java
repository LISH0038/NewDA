package cs451.link;

import cs451.entity.Message;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpLink {
    private DatagramSocket socket;

    public UdpLink(String host, int port) {
        try {
            this.socket = new DatagramSocket(port, InetAddress.getByName(host));
        } catch (Exception e){
            // e.printStackTrace();
        }
    }

    public void send(Message msg)  {
        byte[] buff = Message.serialize(msg);
        try {
            DatagramPacket packet = new DatagramPacket(buff, buff.length, InetAddress.getByName(msg.getDstHost()), msg.getDstPort());
            socket.send(packet);
        } catch (Exception e) {
           // e.printStackTrace();
        }
    }

    public Message receive() {
        byte[] buff = new byte[64];
        DatagramPacket packet = new DatagramPacket(buff,buff.length);
        try {
            socket.receive(packet);
            Message msg = Message.deserialize(buff);
            msg.setSource(packet.getAddress().getHostAddress(), packet.getPort());
            return msg;
        } catch (Exception e) {
            // e.printStackTrace();
            return null;
        }
    }

}
