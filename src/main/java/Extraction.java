import io.mindmaps.graph.config.MindmapsGraphFactory;
import io.mindmaps.graql.api.query.QueryBuilder;
import io.mindmaps.graql.api.query.Result;
import io.mindmaps.graql.api.query.SelectQuery;
import org.apache.tinkerpop.gremlin.structure.Graph;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.mindmaps.graql.api.query.QueryBuilder.var;

/**
 * Methods required to extract information from a mindmaps graph in order to identify the semantics of keywords.
 */

public class Extraction {
    static String graphConf = "/opt/mindmaps/resources/conf/titan-cassandra-server-es.properties";
    static String schema = "http://mindmaps.io/";
    Graph graph;
    QueryBuilder qb;
    public Set<String> conceptNames = new HashSet<>();
    public Map<String,Map<String,Set<String>>> conceptsIID = new HashMap<>();
    public Map<String,Map<String,Long>> conceptsDegree = new HashMap<>();

    public Extraction() {
        graph = MindmapsGraphFactory.buildNewTransaction(graphConf);
        qb = new QueryBuilder(graph, schema);
    }

    public void execute () {
        // add concept types
        conceptNames.add("keyword");
        conceptNames.add("location");
        conceptNames.add("genre");
        conceptNames.add("mood");
        conceptNames.add("person");
        conceptNames.add("company");


        // extract values of concept types
        getValues();

        // write to disk
        try {
            writeIID();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Fetch the degrees using Mindmaps Core until graql can
        getDegrees();

        // write to disk
        try {
            writeDegrees();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void getValues() {
        // collect keywords
        for (String s : conceptNames) {
            System.out.println("Starting to extract values: "+s);
            conceptsIID.put(s, new HashMap<String, Set<String>>());
            SelectQuery query = qb.select("x").where(var("x").isa(s));

            // counter
            int i = 0;

            // clean strings and place as keys in map
            for (Map<String,Result> result : query) {

                // ignore null values
                if (result.get("x").getValue().isPresent()) {
                    String value = result.get("x").getValue().get().toString().trim().replaceAll("\u00A0", "").toLowerCase();
                    if (!conceptsIID.get(s).containsKey(value)) {
                        conceptsIID.get(s).put(value, new HashSet<String>());
                    }
                    conceptsIID.get(s).get(value).add(result.get("x").getId().get());
                }

                i++;
                if (i % 1000 == 0) {
                    System.out.println("current count: " + i);
                }

            }

            System.out.println("Finished extracting - #: " + conceptsIID.get(s).size());
        }
        System.out.println("Done extracting values.");
    }

    private void getDegrees() {
        for (String s : conceptNames) {
            System.out.println("Starting to extract degrees: "+s);
            conceptsDegree.put(s,new HashMap<String,Long>());
            conceptsIID.get(s).values().forEach(b ->  b.forEach(iid ->
                    conceptsDegree.get(s).put(iid,
                    ((long) graph.traversal().V().has("ITEM_IDENTIFIER",iid).values("DEGREE").next()))));
            System.out.println("Finished extracting degrees: "+s);
        }
        System.out.println("Done extracting degrees.");
    }

    private void writeIID() throws IOException {
        System.out.println("Writing to Disk");
        for (String s : conceptNames) {
            Path path = Paths.get("raw/" + s + ".txt");
            Path dirPath = Paths.get("raw");
            if (!Files.exists(dirPath)) {
                Files.createDirectory(dirPath);
                Files.createFile(path);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                conceptsIID.get(s).forEach((a, b) -> {
                    try {
                        writer.write(a);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    b.forEach(c -> {
                        try {
                            writer.write(";" + b);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    try {
                        writer.write("\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                writer.close();
            }
        }
        System.out.println("Finished write.");
    }

    private void writeDegrees() throws IOException {
        System.out.println("Writing to Disk");
        for (String s : conceptNames) {
            Path path = Paths.get("raw/" + s + "Degrees.txt");
            Path dirPath = Paths.get("raw");
            if (!Files.exists(dirPath)) {
                Files.createDirectory(dirPath);
                Files.createFile(path);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                conceptsDegree.get(s).forEach((a,b) -> {
                    try {
                        writer.write(a);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        writer.write(";" + b);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        writer.write("\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
        System.out.println("Finished write.");
    }
}
