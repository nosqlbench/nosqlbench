package io.nosqlbench.activitytype.cqld4.statements.core;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class CQLStatementDef {
    private final static Logger logger = LoggerFactory.getLogger(CQLStatementDef.class);

    private Map<String,String> params = new HashMap<>();
    private String name = "";
    private String statement = "";
    private boolean prepared = true;
    private String cl  = ConsistencyLevel.LOCAL_ONE.name();
    private Map<String, String> bindings = new HashMap<>();

    public CQLStatementDef() {
    }

    public String getGenSpec(String s) {
        return bindings.get(s);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public Map<String, String> getBindings() {
        return bindings;
    }

    public void setBindings(Map<String, String> bindings) {
        this.bindings = bindings;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("  name:").append(this.getName()).append("\n");
        sb.append("  statement: |").append("\n");
        String formattedStmt = Arrays.asList(getStatement().split("\\r*\n"))
                .stream().map(s -> "    " + s)
                .collect(Collectors.joining("\n"));
        sb.append(formattedStmt);
        if (bindings.size() > 0) {
            sb.append("  bindings:\n");
            Optional<Integer> maxLen = this.bindings.keySet().stream().map(String::length).reduce(Integer::max);
            for (String bindName : this.bindings.keySet()) {
                sb
                        .append(String.format("    %-" + (maxLen.orElse(20) + 2) + "s", bindName)).append(" : ")
                        .append(bindings.get(bindName))
                        .append("\n");
            }
        }
        return sb.toString();
    }

    public boolean isPrepared() {
        return prepared;
    }

    public void setPrepared(boolean prepared) {
        this.prepared = prepared;
    }

    public String getConsistencyLevel() {
        return this.cl;
    }

    public void setConsistencyLevel(String consistencyLevel) {
        this.cl = consistencyLevel;
    }


    public void setCl(String consistencyLevel) {
        setConsistencyLevel(consistencyLevel);
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public long getRatio() {
        return Long.parseLong(Optional.ofNullable(params.get("ratio")).orElse("1"));
    }

}
