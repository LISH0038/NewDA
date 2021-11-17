package cs451.link;

import cs451.Host;
import cs451.entity.Message;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;

public class UdpLink {
    private DatagramSocket socket;
    private int selfId;

    public List<Host> dstHosts;
    public HashMap<Integer, Integer> portToIdMap;

    public UdpLink(int hostId, String host, int port, List<Host> dstHosts) {
        try {
            this.socket = new DatagramSocket(port, InetAddress.getByName(host));
        } catch (Exception e){
            // e.printStackTrace();
        }
        this.dstHosts = dstHosts;
        this.selfId = hostId;
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

    public void sendToAll(Message msg)  {
        byte[] buff = msg.serialize();
        for (Host h : this.dstHosts) {
            try {
                DatagramPacket packet = new DatagramPacket(buff, buff.length, InetAddress.getByName(h.getIp()), h.getPort());
                socket.send(packet);
            } catch (Exception e) {
                // e.printStackTrace();
            }
        }
    }

    public Message receive() {
        byte[] buff = new byte[64];
        DatagramPacket packet = new DatagramPacket(buff,buff.length);
        try {
            socket.receive(packet);
            Message msg = new Message(buff);
            int forward = this.portToIdMap.get(packet.getPort());
            msg.setForwardId(forward);
            msg.setDstId(this.selfId);
            msg.setSource(packet.getAddress().getHostAddress(), packet.getPort());
            return msg;
        } catch (Exception e) {
            // e.printStackTrace();
            return null;
        }
    }

}
