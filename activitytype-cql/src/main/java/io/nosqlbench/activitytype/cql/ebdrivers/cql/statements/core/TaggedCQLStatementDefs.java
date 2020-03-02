package io.nosqlbench.activitytype.cql.ebdrivers.cql.statements.core;

import io.nosqlbench.engine.api.util.Tagged;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaggedCQLStatementDefs implements Tagged {

    private List<CQLStatementDef> statements = new ArrayList<>();
    private Map<String,String> tags = new HashMap<>();
    private Map<String,String> params = new HashMap<>();

    public TaggedCQLStatementDefs(Map<String,String> tags, Map<String,String> params, List<CQLStatementDef> statements) {
        this.tags = tags;
        this.params = params;
        this.statements = statements;
    }
    public TaggedCQLStatementDefs(Map<String,String> tags, List<CQLStatementDef> statements) {
        this.tags = tags;
        this.statements = statements;
    }

    public TaggedCQLStatementDefs(List<CQLStatementDef> statements) {
        this.statements = statements;
    }


    public TaggedCQLStatementDefs() {
    }

    public List<CQLStatementDef> getStatements() {
        return statements;
    }

    public void setStatements(List<CQLStatementDef> statements) {
        this.statements = statements;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }
}
