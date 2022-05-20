package io.nosqlbench.activitytype.cql.statements.core;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


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
