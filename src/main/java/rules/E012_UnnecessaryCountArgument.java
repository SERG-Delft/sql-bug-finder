// ERROR 15
package rules;

import interfaces.QueryVisitor;
import net.sf.jsqlparser.expression.Function;

import java.util.ArrayList;

public class E012_UnnecessaryCountArgument extends QueryVisitor {

    private final ArrayList<String> unnecessaryCountArguments = new ArrayList<>();

    public E012_UnnecessaryCountArgument() {
        outputMessage = "unnecessary count argument";
    }

    private void detectUnnecessaryCountArgument(Function function) {
        String expression = function.toString().toLowerCase();
        String func = expression.split("\\(")[0];
        if (func.contains("count") && !expression.contains("distinct") && !expression.contains("*") && !expression.contains("case"))
            unnecessaryCountArguments.add(expression);
    }

    @Override
    public ArrayList<String> getResults() {
        ArrayList<String> results = new ArrayList<>(unnecessaryCountArguments);
        unnecessaryCountArguments.clear();
        return results;
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void visit(Function function) {
        detectUnnecessaryCountArgument(function);
        super.visit(function);
    }
}
