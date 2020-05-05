package io.nosqlbench.activitytype.cql.statements.core;

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

        List<CQLStatementDefParser> defs = new ArrayList<>();
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
