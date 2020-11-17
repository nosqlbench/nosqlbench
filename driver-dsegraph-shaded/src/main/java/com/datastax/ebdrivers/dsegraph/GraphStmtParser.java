package com.datastax.ebdrivers.dsegraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GraphStmtParser {

    private final static Pattern stmtToken = Pattern.compile("\\?(\\w+[-_\\d\\w]*)|\\{(\\w+[-_\\d\\w.]*)}");

    public static List<String> getFields(String statement, Map<String, String> bindings) {

        ArrayList<String> fields = new ArrayList<>();
        Matcher m = stmtToken.matcher(statement);
        while (m.find()) {
            String namedAnchor = m.group(1);
            if (namedAnchor == null) {
                namedAnchor = m.group(2);
                if (namedAnchor == null) {
                    throw new RuntimeException("Pattern '" + stmtToken.pattern() + "' failed to match '" + statement + "'");
                }
            }
            if (!bindings.containsKey(namedAnchor)) {
                throw new RuntimeException("Named anchor " + namedAnchor + " not found in bindings!");
            }
            fields.add(namedAnchor);
        }
        return fields;
    }

    public static String getCookedRepeatedStatement(String statement, int repeat) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < repeat; i++) {
            String varSuffix = String.valueOf(i);
            String indexedStmt = getCookedSuffixedStatement(statement, varSuffix);
            sb.append(indexedStmt);
            sb.append("\n");
        }
        return sb.toString();
    }

    public static String getCookedStatement(String statement) {
        String replaced = stmtToken.matcher(statement).replaceAll("$1$2");
        return replaced;
    }

    public static List<String> getCookedStatements(List<String> statements) {
        return statements.stream().map(GraphStmtParser::getCookedStatement).collect(Collectors.toList());
    }

    public static String getCookedSuffixedStatement(String statement, String suffix) {
        String replaced = stmtToken.matcher(statement).replaceAll("$1" + suffix);
        return replaced;
    }

}
