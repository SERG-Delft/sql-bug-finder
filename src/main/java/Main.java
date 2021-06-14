import db.tables.Queries;
import interfaces.QueryVisitor;
import net.sf.jsqlparser.JSQLParserException;
import utils.QueryReader;
import utils.Rules;

import java.util.*;

public class Main {

    public static void main(String[] args) {
        QueryReader reader = new QueryReader();
        ArrayList<Queries> queries = new ArrayList<>();
        ArrayList<QueryVisitor> rules = (new Rules()).getRules();

        queries.add(new Queries(0L, 0L, 0L, reader.ReadQueryFromConsole(), ""));

        // File file = new File("src/main/resources/queries.txt");
        // queries.addAll(reader.ReadQueriesFromFile(file));

        try {
            for (QueryVisitor rule : rules)
                rule.findBugs(queries);
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
    }
}