package web;

import db.tables.Queries;
import db.tables.Text;
import interfaces.QueryVisitor;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import utils.Rules;

import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class AppController {
    private static final Logger log = LoggerFactory.getLogger(AppController.class);
    CCJSqlParserManager pm = new CCJSqlParserManager();
    @Value("${spring.application.name}")
    String appName;
    String results;

    @GetMapping("/")
    public String homePage(Model model) {
        model.addAttribute("appName", appName);
        model.addAttribute("text", new Text());
        return "home";
    }

    @RequestMapping(value = "/detect", method = RequestMethod.POST)
    public String detect(@ModelAttribute Text text, BindingResult errors, Model model) {
        long id = 1L;
        results = "";
        HashMap<Long, List<String>> bugsMap = new HashMap<>();
        ArrayList<QueryVisitor> rules = (new Rules()).getRules();

        String input = text.getText();
        if (!input.contains(";"))
            input += ";";

        String[] inputQueries = input.split(";");
        ArrayList<Queries> queries = new ArrayList<>();
        for (String query : inputQueries) {
            try {
                pm.parse(new StringReader(query));
                queries.add(new Queries(id++, -1L, -1L, query, "true"));
            } catch (JSQLParserException e) {
                String error = "ERROR while parsing this SQL query, please make sure the syntax is correct!";
                bugsMap.put(id++, new ArrayList<>(Collections.singleton(error)));
            }
        }

        try {
            for (QueryVisitor rule : rules) {
                rule.findBugs(queries);
                HashMap<Long, ArrayList<String>> newBugs = rule.getBugs();
                newBugs.forEach(
                        (key, value) -> bugsMap.merge(key, value, (v1, v2) -> v1.equals(v2) ? v1 : Stream.of(v1, v2).flatMap(Collection::stream).collect(Collectors.toList()))
                );
            }
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }

        for (Map.Entry<Long, List<String>> entry : bugsMap.entrySet()) {
            Long key = entry.getKey();
            for (String bug : entry.getValue()) {
                String result = String.format("[query #%d] %s\n", key, bug);
                results += result;
            }
        }
        model.addAttribute("results", results);
        return "home";
    }
}
