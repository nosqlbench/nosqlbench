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


import io.nosqlbench.engine.api.util.TagFilter;

import java.util.*;
import java.util.stream.Collectors;

public class AvailableCQLStatements {

    private List<TaggedCQLStatementDefs> availableDefs = new ArrayList<>();

    public AvailableCQLStatements(List<TaggedCQLStatementDefs> allStatementDef) {
        this.availableDefs = allStatementDef;
    }

    public List<TaggedCQLStatementDefs> getRawTagged() {
        return availableDefs;
    }

    public Map<String, String> getFilteringDetails(String tagSpec) {
        Map<String, String> details = new LinkedHashMap<>();
        TagFilter ts = new TagFilter(tagSpec);
        for (TaggedCQLStatementDefs availableDef : availableDefs) {
            TagFilter.Result result = ts.matchesTaggedResult(availableDef);
            String names = availableDef.getStatements().stream()
                    .map(CQLStatementDef::getName).collect(Collectors.joining(","));
            details.put(names, result.getLog());
        }
        return details;
    }

    public List<CQLStatementDefParser> getMatching(String tagSpec) {
        TagFilter ts = new TagFilter(tagSpec);
        List<CQLStatementDefParser> CQLStatementDefParsers =
                availableDefs.stream()
                        .filter(ts::matchesTagged)
                        .map(TaggedCQLStatementDefs::getStatements)
                        .flatMap(Collection::stream)
                        .map(p -> new CQLStatementDefParser(p.getName(), p.getStatement()))
                        .collect(Collectors.toList());

        return CQLStatementDefParsers;
    }

    public List<CQLStatementDefParser> getAll() {
        return getMatching("");
    }
}
