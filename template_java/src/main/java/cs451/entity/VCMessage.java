package cs451.entity;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.StringTokenizer;

public class VCMessage extends Message{
    HashMap<Integer, Integer> vc;

    public VCMessage(int type, int source, int seq, int data) {
        super(type, source, seq, data);
    }

    @Override
    public Message newCopy() {
        VCMessage newcopy = new VCMessage(this.type, this.srcId, this.seq, this.payload);
        newcopy.setVc(this.vc);
        return newcopy;
    }

    public static VCMessage deserialize(byte[] data) {
        String raw = new String(data, 0, data.length, StandardCharsets.UTF_8);
        StringTokenizer metas = new StringTokenizer(raw, ":");
        int type =  Integer.parseInt(metas.nextToken());
        int srcId = Integer.parseInt(metas.nextToken());
        int seq = Integer.parseInt(metas.nextToken());
        int payload = Integer.parseInt(metas.nextToken());
        VCMessage vcm = new VCMessage(type, srcId, seq, payload);

        //parsing vc
        String vcRaw = metas.nextToken().trim();
        if (vcRaw.length() > 0) {
            StringTokenizer vcTokens= new StringTokenizer(vcRaw, ";");
            HashMap<Integer, Integer> v = new HashMap<>();
            while (vcTokens.hasMoreTokens()) {
                String[] pair = vcTokens.nextToken().split(",");
                v.put(Integer.parseInt(pair[0]), Integer.parseInt(pair[1]));
            }
            vcm.setVc(v);
        }

        return vcm;
    }

    public void setVc(HashMap<Integer, Integer> vc) {
        this.vc = vc;
    }

    public HashMap<Integer, Integer> getVc() {
        return vc;
    }

    @Override
    public byte[] serialize() {
        StringBuilder sb = new StringBuilder(this.type +
                ":" +
                this.srcId +
                ":" +
                this.seq +
                ":" +
                this.payload +
                ":");
        if (this.vc != null) {
            for (Integer pid: this.vc.keySet()) {
                sb.append(pid).append(",").append(vc.get(pid)).append(";");
            }
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
//        return String.format("%d:%d:%d:%d:", this.type, this.srcId, this.seq, this.payload).getBytes(StandardCharsets.UTF_8);
    }
}
