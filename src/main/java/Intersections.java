import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
        for (LinkedList<String> l : combinations) {
            if (l.size() > 1) {
                System.out.println("computing intersects of: "+l);
                Iterator<String> it = l.iterator();
                Set<String> current = new HashSet<String>(extraction.conceptsIID.get(it.next()).keySet());
                Set<String> clashes = null;
                while (it.hasNext()) {
                    clashes = new HashSet<String>(extraction.conceptsIID.get(it.next()).keySet());
                    current.retainAll(clashes);
                }

                // Write the clashes to a file
                BufferedWriter output;
                try {
                    File file = new File(l.toString()+".txt");
                    output = new BufferedWriter(new FileWriter(file));
                    for (String line : clashes) {
                        output.write(line+"\n");
                    }
                    output.close();
                } catch ( IOException e ) {
                    e.printStackTrace();
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
