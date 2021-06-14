package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;

public class QueryReader {

    public QueryReader() {}

    public ArrayList<String> ReadQueriesFromFile(File file) {
        ArrayList<String> queries = new ArrayList<>();
        try {
            Scanner in = new Scanner(file);
            while (in.hasNextLine()) {
                queries.add(ReadQuery(in));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return queries;
    }

    public ArrayList<String> ReadQueriesFromString(String input) {
        ArrayList<String> queries = new ArrayList<>();
        String[] inputQueries = input.split(";");
        for (String query : inputQueries) {
            query = query.replace("\n", "") + ";";
            queries.add(query);
        }
        return queries;
    }

    public String ReadQueryFromConsole() {
        String query = ReadQuery(new Scanner(System.in));
        System.out.printf("input query: %s\n", query);
        return query;
    }

    public String ReadQuery(Scanner in) {
        StringBuilder query = new StringBuilder();
        while (in.hasNextLine()) {
            String s = in.nextLine();
            query.append(s).append(" ");
            if (s.endsWith(";"))
                break;
        }
        return query.toString();
    }
}
