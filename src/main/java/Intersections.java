import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Compute the intersections of the sets of concept instances. This starts with a root concept type and compares
 * everything else against this. It includes the intersection of multiple sets. For example: keyword vs location vs
 * person vs genre vs ... etc.
 */

public class Intersections {
    Extraction extraction;

    public Intersections(Extraction e){
        extraction = e;
    }

    public void execute() {
        LinkedList<LinkedList<String>> combinations = BinPowSet(new LinkedList<String>(extraction.conceptNames));

        // loop through all possible combinations of concept types
        for (LinkedList<String> l : combinations) {
            if (l.size() > 1) {
                System.out.println("computing intersects of: "+l);

                // compute intersections
                Iterator<String> it = l.iterator();
                Set<String> current = new HashSet<String>(extraction.conceptsIID.get(it.next()).keySet());
                if (current.size()>0) {
                    while (it.hasNext()) {
                        current.retainAll(extraction.conceptsIID.get(it.next()).keySet());
                        System.out.println("current: " + current.size());
                    }
                }

                System.out.println("clashes");
                System.out.println(current.size());

                // Check if file is empty
                if (current.size()>0) {

                    // Write the clashes to a file
                    Path path = Paths.get("clashes/" + l.toString() + ".txt");
                    Path dirPath = Paths.get("clashes");
                    try {
                        if (!Files.exists(dirPath)) {
                            Files.createDirectory(dirPath);
                            Files.createFile(path);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try (BufferedWriter output = Files.newBufferedWriter(path)) {
                        for (String line : current) {
                            output.write(line + "\n");
                        }
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    private <T extends Comparable<? super T>> LinkedList<LinkedList<T>> BinPowSet(
            LinkedList<T> A){
        LinkedList<LinkedList<T>> ans= new LinkedList<LinkedList<T>>();
        int ansSize = (int)Math.pow(2, A.size());
        for(int i= 0;i< ansSize;++i){
            String bin= Integer.toBinaryString(i); //convert to binary
            while(bin.length() < A.size()) bin = "0" + bin; //pad with 0's
            LinkedList<T> thisComb = new LinkedList<T>(); //place to put one combination
            for(int j= 0;j< A.size();++j){
                if(bin.charAt(j) == '1')thisComb.add(A.get(j));
            }
            Collections.sort(thisComb); //sort it for easy checking
            ans.add(thisComb); //put this set in the answer list
        }
        return ans;
    }

}
