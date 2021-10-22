package cs451;

import java.io.Serializable;

public class Message implements Serializable {
    private boolean isMsg;
    private int source;
    private int seq;
    private String srcHost;
    private int srcPort;
    private String dstHost;
    private int dstPort;

    Message(boolean isMsg, int source, int seq) {
        this.isMsg = isMsg;
        this.source = source;
        this.seq = seq;
    }

    int getSeq(){
        return this.seq;
    }

    boolean isMsg() {
        return this.isMsg;
    }

    int getSource() {
        return source;
    }

    String getDstHost() {
        return dstHost;
    }

    int getDstPort() {
        return dstPort;
    }

    String getSrcHost() {
        return srcHost;
    }

    int getSrcPort() {
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

//    @Override
//    public int hashCode() {
//        return this.getSeq();
//    }
//
//    @Override
//    public boolean equals(Object ob) {
//        if (this.getSeq() == ((Message) ob).getSeq()) {
//            return true;
//        }
//        return false;
//    }
}