package utils;

import interfaces.QueryVisitor;
import rules.*;

import java.util.ArrayList;

public class Rules {

    private final ArrayList<QueryVisitor> rules = new ArrayList<>();

    public Rules() {
        rules.add(new E001_InconsistentTupleVariable());
        rules.add(new E002_ConstantOutputColumn());
        rules.add(new E003_DuplicateOutputColumn());
        rules.add(new E004_UnnecessaryTupleVariable());
        rules.add(new E005_IdenticalTupleVariable());
        rules.add(new E006_ComparisonWithNull());
        rules.add(new E007_UnnecessaryGeneralComparison());
        rules.add(new E008_LikeWithoutWildcards());
        rules.add(new E009_UnnecessarySelectListInExists());
        rules.add(new E010_UnnecessaryIndexScan());
        rules.add(new E011_UnnecessaryDistinctInAggregations());
        rules.add(new E012_UnnecessaryCountArgument());
        rules.add(new E013_UnnecessaryGroupByAttribute());
        rules.add(new E014_UnnecessaryGroupByClause());
        rules.add(new E015_UnnecessaryUnionCondition());
        rules.add(new E016_UnnecessaryOrderByTerm());
        rules.add(new E017_UnnecessaryGroupByTerm());
        rules.add(new E018_InefficientHavingAttribute());
        rules.add(new E019_MissingJoinPredicate());
        rules.add(new E020_MissingJoinInExistsCondition());
        rules.add(new E021_MismatchInSubQuerySelectCondition());
        rules.add(new E022_StrangeSubQueryCondition());
        rules.add(new E023_StrangeHavingClause());
        rules.add(new E024_StrangeWildcardsWithoutLike());
        rules.add(new E025_DivisionByZero());
    }

    public ArrayList<QueryVisitor> getRules() {
        return rules;
    }
}
