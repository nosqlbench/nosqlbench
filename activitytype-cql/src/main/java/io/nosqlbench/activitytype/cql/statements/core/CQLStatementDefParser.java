package io.nosqlbench.activitytype.cql.statements.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CQLStatementDefParser {
    private final static Logger logger = LoggerFactory.getLogger(CQLStatementDefParser.class);
    //    private final static Pattern templateToken = Pattern.compile("<<(\\w+(:(.+?))?)>>");
    private final static Pattern stmtToken = Pattern.compile("\\?(\\w+[-_\\d\\w]*)|\\{(\\w+[-_\\d\\w.]*)}");
    private final static String UNSET_VALUE = "UNSET-VALUE";
    private final String stmt;
    private final String name;

    private CQLStatementDef deprecatedDef; // deprecated, to be removed

    public void setBindings(Map<String, String> bindings) {
        this.bindings = bindings;
    }

    private Map<String, String> bindings;

    public CQLStatementDef getDeprecatedDef() {
        return deprecatedDef;
    }

    public void setDeprecatedDef(CQLStatementDef deprecatedDef) {
        this.deprecatedDef = deprecatedDef;
    }

    public CQLStatementDefParser(String name, String stmt) {
        this.stmt = stmt;
        this.name = name;
        this.bindings = bindings;
    }

    public Map<String,String> getBindings() {
        return bindings;
    }

    /**
     * @return bindableNames in order as specified in the parameter placeholders
     */
    public List<String> getBindableNames() {
        Matcher m = stmtToken.matcher(stmt);
        List<String> bindNames = new ArrayList<>();
        while (m.find()) {
            String form1 = m.group(1);
            String form2 = m.group(2);
            bindNames.add( (form1!=null && !form1.isEmpty()) ? form1 : form2 );
        }
        return bindNames;
    }

    public String getName() {
        return name;
    }


    public String getParsedStatementOrError(Set<String> namedBindings) {
        ParseResult result = getParseResult(namedBindings);
        if (result.hasError()) {
            throw new RuntimeException("Statement template has errors:\n" + result.toString());
        }
        return result.getStatement();
    }

    public ParseResult getParseResult(Set<String> namedBindings) {

        HashSet<String> missingAnchors = new HashSet<String>() {{ addAll(namedBindings); }};
        HashSet<String> missingBindings = new HashSet<String>();

        String statement = this.stmt;
        StringBuilder cooked = new StringBuilder();

        Matcher m = stmtToken.matcher(statement);
        int lastMatch = 0;
        String remainder = "";
        while (m.find(lastMatch)) {
            String pre = statement.substring(lastMatch, m.start());

            String form1 = m.group(1);
            String form2 = m.group(2);
            String tokenName = (form1!=null && !form1.isEmpty()) ? form1 : form2;
            lastMatch = m.end();
            cooked.append(pre);
            cooked.append("?");

            if (!namedBindings.contains(tokenName)) {
                missingBindings.add(tokenName);
            } else {
                if (missingAnchors.contains(tokenName)) {
                    missingAnchors.remove(tokenName);
                }
            }

        }

        // add remainder of unmatched
        if (lastMatch>=0) {
            cooked.append(statement.substring(lastMatch));
        }
        else {
            cooked.append(statement);
        }

        logger.info("Parsed statement as: " + cooked.toString().replaceAll("\\n","\\\\n"));

        return new ParseResult(cooked.toString(),name,bindings,missingBindings,missingAnchors);
    }

    public static class ParseResult {
        private Set<String> missingGenerators;
        private Set<String> missingAnchors;
        private String statement;
        private Map<String,String> bindings;
        private String name;

        public ParseResult(String stmt, String name, Map<String,String> bindings, Set<String> missingGenerators, Set<String> missingAnchors) {
            this.missingGenerators = missingGenerators;
            this.missingAnchors = missingAnchors;
            this.statement = stmt;
            this.name = name;
        }

        public String toString() {
            String generatorsSummary = (this.missingGenerators.size() > 0) ?
                    "\nundefined generators:" + this.missingGenerators.stream().collect(Collectors.joining(",", "[", "]")) : "";
            return "STMT:" + statement + "\n" + generatorsSummary;
        }

        public String getName() {
            return name;
        }

        public Map<String,String> getBindings() {
            return bindings;
        }

        public boolean hasError() {
            return missingGenerators.size() > 0;
        }

        public String getStatement() {
            return statement;
        }

        public Set<String> getMissingAnchors() {
            return missingAnchors;
        }

        public Set<String> getMissingGenerators() {
            return missingGenerators;
        }
    }

}
