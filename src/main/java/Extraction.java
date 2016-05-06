import io.mindmaps.graph.config.MindmapsGraphFactory;
import io.mindmaps.graql.api.query.QueryBuilder;
import io.mindmaps.graql.api.query.SelectQuery;
import org.apache.tinkerpop.gremlin.structure.Graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.mindmaps.graql.api.query.QueryBuilder.var;

/**
 * Methods required to extract information from a mindmaps graph in order to identify the semantics of keywords.
 */

public class Extraction {
    static String graphConf = "/opt/mindmaps/resources/conf/titan-cassandra.properties";
    static String schema = "http://mindmaps.io/";
    Graph graph;
    QueryBuilder qb;
    Set<String> conceptNames = new HashSet<>();
    Map<String,Map<String,String>> conceptsIID = new HashMap<>();
    Map<String,Map<String,Long>> conceptsDegree = new HashMap<>();

    public Extraction() {
        graph = MindmapsGraphFactory.buildNewTransaction(graphConf);
        qb = new QueryBuilder(graph, schema);
    }

    public void execute () {
        // add concept types
        conceptNames.add("keyword");
//        conceptNames.add("location");
//        conceptNames.add("genre");
//        conceptNames.add("mood");
//        conceptNames.add("person");
//        conceptNames.add("company");

        // collect keywords
        for (String s : conceptNames) {
            conceptsIID.put(s, new HashMap<String, String>());
            SelectQuery query = qb.select("x").where(var("x").isa(s));

            // clean strings and place as keys in map
            query.forEach(
                    result -> conceptsIID.get(s).put(
                            result.get("x").getValue().get().toString().trim().replaceAll("\u00A0", "").toLowerCase(),
                            result.get("x").getId().get()));
        }

        getDegrees();

//        conceptsIID.get("keyword").forEach((a,b) -> System.out.println(a + b));
        conceptsDegree.get("keyword").forEach((a,b)-> System.out.println(a + b));
        System.out.println(conceptsIID.get("keyword").get("snowboarding competition"));
    }

    private void getDegrees() {
        for (String s : conceptNames) {
            conceptsDegree.put(s,new HashMap<String,Long>());
            conceptsIID.get(s).forEach(
                    (a, b) -> conceptsDegree.get(s).put(a, ((long) graph.traversal().V().has("ITEM_IDENTIFIER",b).values("DEGREE").next())));
        }
    }
}
