package cs451.link;

import cs451.Host;
import cs451.entity.Message;
import cs451.entity.VCMessage;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;

public class UdpLink {
    private DatagramSocket socket;

    public HashMap<Integer, Integer> portToIdMap;

    public UdpLink(int hostId, String host, int port, List<Host> dstHosts) {
        try {
            this.socket = new DatagramSocket(port, InetAddress.getByName(host));
        } catch (Exception e){
            // e.printStackTrace();
        }
        this.portToIdMap = new HashMap<>();
        for (Host h: dstHosts) {
            this.portToIdMap.put(h.getPort(), h.getId());
        }
    }

    public void send(Message msg)  {
        byte[] buff = msg.serialize();
        try {
            DatagramPacket packet = new DatagramPacket(buff, buff.length, InetAddress.getByName(msg.getDstHost()), msg.getDstPort());
            socket.send(packet);
        } catch (Exception e) {
           // e.printStackTrace();
        }
    }

    public Message receive() {
        byte[] buff = new byte[32];
        DatagramPacket packet = new DatagramPacket(buff,buff.length);
        try {
            socket.receive(packet);
//            Message msg = new Message(buff);
            Message msg = VCMessage.deserialize(buff);
            int forward = this.portToIdMap.get(packet.getPort());
            msg.setForwardId(forward);
            if (msg.getType() != 0) {
                msg.setSource(packet.getAddress().getHostAddress(), packet.getPort());
            }
            return msg;
        } catch (Exception e) {
            // e.printStackTrace();
            return null;
        }
    }

}
