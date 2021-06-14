// ERROR 14
// ERROR 30
package rules;

import interfaces.QueryVisitor;
import net.sf.jsqlparser.expression.Function;

import java.util.ArrayList;
import java.util.Arrays;

public class E011_UnnecessaryDistinctInAggregations extends QueryVisitor {

    private final ArrayList<String> unnecessaryDistinct = new ArrayList<>();
    private final ArrayList<String> aggregationFunctions = new ArrayList<>(Arrays.asList("max", "min", "sum", "avg"));

    public E011_UnnecessaryDistinctInAggregations() {
        outputMessage = "unnecessary distinct in aggregate function";
    }

    @Override
    public ArrayList<String> getResults() {
        ArrayList<String> results = new ArrayList<>(unnecessaryDistinct);
        cleanup();
        return results;
    }

    @Override
    public void cleanup() {
        unnecessaryDistinct.clear();
    }

    @Override
    public void visit(Function function) {
        String expression = function.toString().toLowerCase();
        String func = expression.split("\\(")[0];
        if (aggregationFunctions.contains(func) && function.getParameters() != null && function.isDistinct())
            unnecessaryDistinct.add(function.toString());
    }
}
