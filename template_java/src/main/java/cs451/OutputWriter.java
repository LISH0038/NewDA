package cs451;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

// use as singleton
class OutputWriter {
//    private static OutputWriter single_instance = null;
    private static StringBuilder sb = new StringBuilder();
    private static BufferedWriter bw;

//    private OutputWriter(String path) throws IOException {
//        bw = new BufferedWriter(new FileWriter(path));
//    }

//    static OutputWriter getInstance(String path) throws IOException {
//        if (single_instance == null)
//            single_instance = new OutputWriter(path);
//
//        return single_instance;
//    }

    static synchronized void addLineToOutputBuffer(String s) {
        sb.append(s).append("\n");
    }

    static void outputToFile(String path){
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
