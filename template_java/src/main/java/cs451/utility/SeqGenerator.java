package cs451.utility;

public class SeqGenerator {
    private static int seq = 0;

    public static int generateSeqNum(){
        return ++seq;
    }
}
