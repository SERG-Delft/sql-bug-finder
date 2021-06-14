package db;

import db.repositories.*;
import db.tables.Bugs;
import db.tables.EvoSqlBugs;
import db.tables.EvoSqlQueries;
import db.tables.Queries;
import interfaces.QueryVisitor;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import rules.E000_ComputeQueryComplexity;
import utils.Rules;

import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
public class DbApp implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DbApp.class);
    @Autowired
    private OwnersRepository ownersRepository;
    @Autowired
    private QuestionsRepository questionsRepository;
    @Autowired
    private AnswersRepository answersRepository;
    @Autowired
    private QueriesRepository queriesRepository;
    @Autowired
    private EvoSqlQueriesRepository evosqlQueriesRepository;
    @Autowired
    private PagesRepository pagesRepository;
    @Autowired
    private BugsRepository bugsRepository;
    @Autowired
    private EvoSqlBugsRepository evosqlBugsRepository;

    public static void main(String[] args) {
        SpringApplication.run(DbApp.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        validateUnknownQueries();
        runRulesOnValidQueries();
        // computeQueryComplexity();
    }

    private void computeQueryComplexity() {
        log.info("calling method: queriesRepository.findValidQueries()");
        ArrayList<Queries> queries = new ArrayList<>(queriesRepository.findValidQueries());
        // ArrayList<EvoSqlQueries> queries = new ArrayList<>(evosqlQueriesRepository.findValidQueries());
        E000_ComputeQueryComplexity rule = new E000_ComputeQueryComplexity();
        int count = 0;
        try {
            rule.findBugs(queries);
            // rule.findEvoSqlBugs(queries);
            HashMap<Long, ArrayList<String>> bugs = rule.getBugs();
            log.info("done computing queries complexity");
            for (Queries query : queries) {
                count += 1;
                ArrayList<String> properties = bugs.get(query.getQueryId());
                String predicates = properties.get(0).split("predicates: ")[1];
                String joins = properties.get(1).split("joins: ")[1];
                String subqueries = properties.get(2).split("subqueries: ")[1];
                String functions = properties.get(3).split("functions: ")[1];
                String columns = properties.get(4).split("columns: ")[1];
                query.setPredicates(Long.parseLong(predicates));
                query.setJoins(Long.parseLong(joins));
                query.setSubqueries(Long.parseLong(subqueries));
                query.setFunctions(Long.parseLong(functions));
                query.setColumns(Long.parseLong(columns));
                if (count % 1000 == 0)
                    log.info(String.format("done computing complexity for %s queries", count));
            }
            log.info("updating queries complexity and saving to db");
            queriesRepository.saveAll(queries);
            // evosqlQueriesRepository.saveAll(queries);
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
    }

    private void validateUnknownQueries() {
        log.info("calling method: queriesRepository.findUnknownQueries()");
        // ArrayList<String> queries = queriesRepository.findUnknownQueries().stream().map(Queries::getQuery).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Queries> queries = new ArrayList<>(queriesRepository.findUnknownQueries());
        // ArrayList<EvoSqlQueries> queries = new ArrayList<>(evosqlQueriesRepository.findUnknownQueries());
        log.info(String.format("found %s unknown queries", queries.size()));
        log.info("validating unknown queries");
        CCJSqlParserManager pm = new CCJSqlParserManager();
        int validQueries = 0, invalidQueries = 0;
        for (Queries query : queries) {
            // log.info(String.format("parsing query: %s", query.getQuery()));
            try {
                pm.parse(new StringReader(query.getQuery()));
                query.setIsValid("1");
                validQueries += 1;
            } catch(JSQLParserException e) {
                query.setIsValid("0");
                invalidQueries += 1;
            }
            query.setPredicates(0L);
            query.setJoins(0L);
            query.setSubqueries(0L);
            query.setFunctions(0L);
            query.setColumns(0L);
            if ((validQueries + invalidQueries) % 1000 == 0)
                log.info(String.format("done validating %s queries, valid: %s, invalid: %s", validQueries + invalidQueries, validQueries, invalidQueries));
        }
        log.info(String.format("done validating queries, found %s valid and %s invalid", validQueries, invalidQueries));
        // evosqlQueriesRepository.saveAll(queries);
        queriesRepository.saveAll(queries);
    }

    private void runRulesOnValidQueries() {
        ArrayList<QueryVisitor> rules = (new Rules()).getRules();
        log.info("calling method: queriesRepository.findValidQueries()");
        // List<String> queries = queriesRepository.findValidQueries().stream().map(Queries::getQuery).collect(Collectors.toList());
        ArrayList<Queries> queries = new ArrayList<>(queriesRepository.findValidQueries());
        ArrayList<Bugs> bugs = new ArrayList<>();
        // ArrayList<EvoSqlQueries> queries = new ArrayList<>(evosqlQueriesRepository.findValidQueries());
        // ArrayList<EvoSqlBugs> bugs = new ArrayList<>();
        HashMap<Long, List<String>> bugsMap = new HashMap<>();
        HashMap<Long, List<String>> errorsMap = new HashMap<>();
        try {
            for (QueryVisitor rule : rules) {
                rule.findBugs(queries);
                // rule.findEvoSqlBugs(queries);
                HashMap<Long, ArrayList<String>> newBugs = rule.getBugs();
                HashMap<Long, ArrayList<String>> newErrors = rule.getErrors();
                newBugs.forEach(
                    (key, value) -> bugsMap.merge(key, value, (v1, v2) -> v1.equals(v2) ? v1 : Stream.of(v1, v2).flatMap(Collection::stream).collect(Collectors.toList()))
                );
                newErrors.forEach(
                    (key, value) -> errorsMap.merge(key, value, (v1, v2) -> v1.equals(v2) ? v1 : Stream.of(v1, v2).flatMap(Collection::stream).collect(Collectors.toList()))
                );
                log.info(String.format("done checking rule: %s", rule.getClass().getName()));
            }
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
        log.info("adding detected bugs to db");
        for (Map.Entry<Long, List<String>> entry : bugsMap.entrySet())
            bugs.add(new Bugs(entry.getKey(), entry.getValue().toString(), errorsMap.get(entry.getKey()).toString()));
        bugsRepository.saveAll(bugs);
        // evosqlBugsRepository.saveAll(bugs);
    }
}
