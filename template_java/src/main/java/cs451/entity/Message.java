package cs451.entity;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

public class Message implements Serializable {
    // data to transmit
    private int type; // 0 - Ack, 1 - Msg, 2 - Broadcast
    private int srcId;
    private int seq;
    private int data;

    // auxiliary information
    private String forwardHost;
    private int forwardPort;
    private int forwardId;
    private String dstHost;
    private int dstPort;
    private int dstId;

    // identifiers
    private String msgId; // uniquely identify link level msg
    private String uId; // uniquely identify broadcast level msg

    public Message(int type, int source, int seq, int data) {
        this.type = type;
        this.srcId = source;
        this.seq = seq;
        this.data = data;
    }

    public Message(byte[] data) {
        String raw = new String(data, 0, data.length, StandardCharsets.UTF_8);
        String[] metas = raw.split(":");
        this.type = Integer.parseInt(metas[0]);
        this.srcId = Integer.parseInt(metas[1]);
        this.seq = Integer.parseInt(metas[2]);
        this.data = Integer.parseInt(metas[3]);
    }

    public int getData(){
        return this.data;
    }

    public int getSeq() {
        return seq;
    }

    public int getType() {
        return this.type;
    }

    public int getSrcId() {
        return srcId;
    }

    public String getDstHost() {
        return dstHost;
    }

    public int getDstPort() {
        return dstPort;
    }

    public String getForwardHost() {
        return forwardHost;
    }

    public int getForwardPort() {
        return forwardPort;
    }

    public String getMsgid() {
        if (this.msgId == null) {
            this.msgId = this.type == 0 ? this.srcId +":"+ this.seq+":"+ this.dstId +","+ this.forwardId
                    : this.srcId +":"+ this.seq+":"+ this.forwardId +","+ this.dstId;
        }
        return this.msgId;
    }

    public String getUid() {
        if (this.uId == null) {
            this.uId = this.srcId + "," + this.seq;
        }
        return uId;
    }

    public int getForwardId() {
        return forwardId;
    }

    public void setSource(String srcHost, int srcPort) {
        this.forwardHost = srcHost;
        this.forwardPort = srcPort;
    }

    public void setDestination(String dstHost, int dstPort) {
        this.dstHost = dstHost;
        this.dstPort = dstPort;
    }

    public void setForwardId(int forwardId) {
        this.forwardId = forwardId;
    }

    public void setDstId(int dstId) {
        this.dstId = dstId;
    }

    public void setType(int type) {
        this.type = type;
    }

    public byte[] serialize() {
        return String.format("%d:%d:%d:%d:", this.type, this.srcId, this.seq, this.data).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public int hashCode() {
        return this.getSeq();
    }

    @Override
    public boolean equals(Object ob) {
        return this.getSeq() == ((Message) ob).getSeq();
    }
}