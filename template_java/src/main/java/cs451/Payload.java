package cs451;

import java.io.Serializable;

public class Payload implements Serializable {
    private boolean isMsg;
    private int source;
    private int seq;

    public Payload(boolean isMsg, int source, int seq) {
        this.isMsg = isMsg;
        this.source = source;
        this.seq = seq;
    }

    public void setSeq(int seq) {
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
}