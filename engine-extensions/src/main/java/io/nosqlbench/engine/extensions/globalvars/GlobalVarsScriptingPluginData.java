package io.nosqlbench.engine.extensions.globalvars;

import com.codahale.metrics.MetricRegistry;
import io.nosqlbench.engine.api.extensions.ScriptingPluginInfo;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;
import org.slf4j.Logger;

import javax.script.ScriptContext;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalVarsScriptingPluginData implements ScriptingPluginInfo<ConcurrentHashMap<String,Object>> {
    @Override
    public String getDescription() {
        return "The global access map from shared state";
    }

    @Override
    public ConcurrentHashMap<String, Object> getExtensionObject(Logger logger, MetricRegistry metricRegistry, ScriptContext scriptContext) {
        return SharedState.gl_ObjectMap;
    }

    @Override
    public String getBaseVariableName() {
        return "globalvars";
    }
}
