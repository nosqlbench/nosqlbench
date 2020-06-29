package io.nosqlbench.engine.api.activityconfig.yaml;

import io.nosqlbench.engine.api.activityconfig.ParsedStmt;
import io.nosqlbench.engine.api.util.Tagged;

import java.util.Map;
import java.util.Optional;

public interface OpTemplate extends Tagged {

    String getName();

    String getStmt();

    Map<String,String> getBindings();

    Map<String, Object> getParams();

    <T> Map<String,T> getParamsAsValueType(Class<? extends T> type);

    @SuppressWarnings("unchecked")
    <V> V getParamOrDefault(String name, V defaultValue);

    <V> V getParam(String name, Class<? extends V> type);

    @SuppressWarnings("unchecked")
    <V> Optional<V> getOptionalParam(String name, Class<? extends V> type);

    Optional<String> getOptionalParam(String name);

    Map<String,String> getTags();

    /**
     * Parse the statement for anchors and return a richer view of the StmtDef which
     * is simpler to use for most statement configuration needs.
     * @return a new {@link ParsedStmt}
     */
    ParsedStmt getParsed();

    String getDesc();
}
