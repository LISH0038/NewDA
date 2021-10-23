package cs451.utility;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class OutputWriter {
    private static StringBuilder sb = new StringBuilder();
    private static BufferedWriter bw;

    public static synchronized void addLineToOutputBuffer(String s) {
        sb.append(s).append("\n");
    }

    public static void outputToFile(String path){
        try {
            bw = new BufferedWriter(new FileWriter(path));
            bw.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
