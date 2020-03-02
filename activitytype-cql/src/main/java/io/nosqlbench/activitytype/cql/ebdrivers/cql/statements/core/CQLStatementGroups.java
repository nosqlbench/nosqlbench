package io.nosqlbench.activitytype.cql.ebdrivers.cql.statements.core;

import java.util.*;

public class CQLStatementGroups {

    private Map<String,List<CQLStatementDefParser>> statementGroups = new HashMap<>();

    public CQLStatementGroups(Map<String,List<CQLStatementDefParser>> statementGroups) {
        this.statementGroups = statementGroups;

    }

    public List<CQLStatementDefParser> getGroups(String... groupNames) {
        List<CQLStatementDefParser> statements = new ArrayList<CQLStatementDefParser>();
        for (String groupName : groupNames) {
            List<CQLStatementDefParser> adding = statementGroups.getOrDefault(groupName, Collections.emptyList());
            statements.addAll(adding);
        }
        return statements;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        List<String> groups = new ArrayList<String>(statementGroups.keySet());
        Collections.sort(groups);
        sb.append("groups:\n");
        for (String group : groups) {
//            sb.append("section:").append(section).append("\n");
            for (CQLStatementDefParser statementDef : statementGroups.get(group)) {
                sb.append(statementDef.toString());
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
