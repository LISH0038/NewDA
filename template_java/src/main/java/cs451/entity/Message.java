package cs451.entity;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

public class Message implements Serializable {
    // state: 0 - Ack, 1 - Msg, 2 - Broadcast
    private int type;
    private boolean sent;
    private int source;
    private int id;
    private int data;
    private String srcHost;
    private int srcPort;
    private String dstHost;
    private int dstPort;

    public Message(int type, int source, int id, int data) {
        this.type = type;
        this.source = source;
        this.id = id;
        this.data = data;
    }

    public Message(byte[] data) {
        String raw = new String(data, 0, data.length, StandardCharsets.UTF_8);
        String[] metas = raw.substring(1).split(":");

        this.type = Integer.parseInt(metas[0]);
        this.source = Integer.parseInt(metas[1]);
        this.id = Integer.parseInt(metas[2]);
        this.data = Integer.parseInt(metas[3]);
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

    public int getType() {
        return this.type;
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

    public byte[] serialize() {
        return String.format("%d:%d:%d:%d:", this.type, this.source, this.id, this.data).getBytes(StandardCharsets.UTF_8);
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