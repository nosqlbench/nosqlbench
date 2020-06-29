package io.nosqlbench.activitytype.cmds;

import io.nosqlbench.engine.api.activityconfig.ParsedStmt;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtDef;
import io.nosqlbench.engine.api.activityimpl.motor.ParamsParser;
import io.nosqlbench.engine.api.templating.CommandTemplate;
import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.virtdata.core.bindings.BindingsTemplate;
import io.nosqlbench.virtdata.core.templates.ParsedTemplate;
import io.nosqlbench.virtdata.core.templates.StringBindings;
import io.nosqlbench.virtdata.core.templates.StringBindingsTemplate;

import java.net.http.HttpRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.LongFunction;
import java.util.stream.Collectors;

public class ReadyHttpRequest implements LongFunction<HttpRequest> {

    private final static HttpRequestSetter setter = new HttpRequestSetter();

    public enum FieldType {
        method,
        port,
        host,
        path,
        query,
        header,
        version
    }

    HttpRequest.Builder builder = HttpRequest.newBuilder();
    Map<FieldType, StringBindings> unresolved = new HashMap<>();

    // only populated if there is no value which is an actual bindings template
    private final HttpRequest cachedRequest;

    public ReadyHttpRequest(StmtDef stmtDef) {
        CommandTemplate cmdt = new CommandTemplate(stmtDef, false);
        ParsedStmt parsed = stmtDef.getParsed();

        Map<String, String> reqParams = new HashMap<>();

        String stmt = parsed.getStmt();
        if (stmt != null) {
            Map<String, String> parsedparams = ParamsParser.parse(stmt, false);
            reqParams.putAll(parsedparams);
        }
        for (String paramsKey : stmtDef.getParams().keySet()) {
            if (reqParams.containsKey(paramsKey)) {
                throw new RuntimeException("request parameter '" + paramsKey + "' used again in params block. Choose one.");
            }
        }
        reqParams.putAll(stmtDef.getParamsAsValueType(String.class));

        for (String cfgname : reqParams.keySet()) {
            FieldType cfgfield;
            try {
                cfgfield = FieldType.valueOf(cfgname);
            } catch (IllegalArgumentException iae) {
                throw new BasicError("You can't configure a request with '" + cfgname + "'." +
                        " Valid properties are " + Arrays.stream(FieldType.values()).map(String::valueOf).collect(Collectors.joining(",")));
            }
            String value = reqParams.get(cfgname);
            ParsedTemplate tpl = new ParsedTemplate(value, stmtDef.getBindings());
            if (tpl.getBindPoints().size() == 0) {
                builder = setter.setField(builder, cfgfield, value);
            } else {
                BindingsTemplate bindingsTemplate = new BindingsTemplate(tpl.getBindPoints());
                StringBindingsTemplate stringBindingsTemplate = new StringBindingsTemplate(value, bindingsTemplate);
                StringBindings stringBindings = stringBindingsTemplate.resolve();
                unresolved.put(cfgfield, stringBindings);
            }
        }

        if (unresolved.size() == 0) {
            cachedRequest = builder.build();
        } else {
            cachedRequest = null;
        }
    }

    @Override
    public HttpRequest apply(long value) {
        if (this.cachedRequest != null) {
            return this.cachedRequest;
        }
        HttpRequest.Builder newRq = builder.copy();
        for (Map.Entry<FieldType, StringBindings> toset : unresolved.entrySet()) {
            String setValue = toset.getValue().bind(value);
            newRq = setter.setField(newRq, toset.getKey(), setValue);
        }

        HttpRequest request = newRq.build();
        return request;
    }
}
