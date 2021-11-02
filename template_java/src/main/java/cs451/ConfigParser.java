package cs451;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ConfigParser {

    private static final String SPACES_REGEX = "\\s+";

    private String path;
    private int m;
    private int i;

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

                m = Integer.parseInt(splits[0]);
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

    public int getI() { return i; }

}
