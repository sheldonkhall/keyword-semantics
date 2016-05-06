/**
 *
 */

public class main {
    public static void main(String [ ] args) {
        Extraction e = new Extraction();
        e.execute();
        Intersections i = new Intersections(e);
        i.execute();
        System.exit(0);
    }
}
