package cs451;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ConfigParser {

    private static final String SPACES_REGEX = "\\s+";

    private String path;
    private int m;
    private final HashMap<Integer, ArrayList<Integer>> causalMap = new HashMap<>();

    public boolean populate(String filename) {
        File file = new File(filename);
        path = file.getPath();
        try(BufferedReader br = new BufferedReader(new FileReader(filename))) {
            int lineNum = 1;
            for(String line; (line = br.readLine()) != null; lineNum++) {
                if (line.isBlank()) {
                    continue;
                }

                String[] splits = line.split(SPACES_REGEX);
//                if (splits.length != 2) {
//                    System.err.println("Problem with the line " + lineNum + " in the config file!");
//                    return false;
//                }
                if (lineNum == 1) {
                    m = Integer.parseInt(splits[0]);
                } else {
                    int id = Integer.parseInt(splits[0]);
                    ArrayList<Integer> causal = new ArrayList<>();
                    for (int j = 1; j < splits.length; j ++) {
                        causal.add(Integer.parseInt(splits[j]));
                    }
                    causalMap.put(id, causal);
                }
                // i = Integer.parseInt(splits[1]);
            }
        } catch (IOException e) {
            System.err.println("Problem with the config file!");
            return false;
        }

        return true;
    }

    public String getPath() {
        return path;
    }

    public int getM() { return m; }

//    public int getI() { return i; }

    public HashMap<Integer, ArrayList<Integer>> getCausalMap() { return causalMap; }

}
