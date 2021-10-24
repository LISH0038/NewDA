package cs451.entity;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

public class Message implements Serializable {
    private boolean isMsg;
    private boolean sent;
    private int source;
    private int id;
    private int data;
    private String srcHost;
    private int srcPort;
    private String dstHost;
    private int dstPort;

    public Message(boolean isMsg, int source, int id, int data) {
        this.isMsg = isMsg;
        this.source = source;
        this.id = id;
        this.data = data;
    }

    public int getData(){
        return this.data;
    }

    public int getId() {
        return id;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public boolean isMsg() {
        return this.isMsg;
    }

    public int getSource() {
        return source;
    }

    public String getDstHost() {
        return dstHost;
    }

    public int getDstPort() {
        return dstPort;
    }

    public String getSrcHost() {
        return srcHost;
    }

    public int getSrcPort() {
        return srcPort;
    }

    public void setSource(String srcHost, int srcPort) {
        this.srcHost = srcHost;
        this.srcPort = srcPort;
    }

    public void setDestination(String dstHost, int dstPort) {
        this.dstHost = dstHost;
        this.dstPort = dstPort;
    }

    public static byte[] serialize(Message m) {
        int isMsg = m.isMsg()? 1 : 0;
        return String.format("%d%d:%d:%d:", isMsg, m.getSource(), m.getId(), m.getData()).getBytes(StandardCharsets.UTF_8);
    }

    public static Message deserialize(byte[] data) {
        String raw = new String(data, 0, data.length, StandardCharsets.UTF_8);
        boolean isMsg = raw.charAt(0) == '1';
        String[] metas = raw.substring(1).split(":");

        return new Message(isMsg, Integer.parseInt(metas[0]), Integer.parseInt(metas[1]), Integer.parseInt(metas[2]));
    }

    @Override
    public int hashCode() {
        return this.getId();
    }

    @Override
    public boolean equals(Object ob) {
        return this.getId() == ((Message) ob).getId();
    }
}