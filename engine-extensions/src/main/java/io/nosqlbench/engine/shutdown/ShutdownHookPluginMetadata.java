package io.nosqlbench.engine.shutdown;

import com.codahale.metrics.MetricRegistry;
import io.nosqlbench.engine.api.extensions.ScriptingPluginInfo;
import io.nosqlbench.nb.annotations.Service;
import org.apache.logging.log4j.Logger;

import javax.script.ScriptContext;

@Service(value=ScriptingPluginInfo.class,selector = "shutdown")
public class ShutdownHookPluginMetadata implements ScriptingPluginInfo<ShutdownHookPlugin> {

    @Override
    public String getDescription() {
        return "Register shutdown hooks in the form of javascript functions.";
    }

    @Override
    public ShutdownHookPlugin getExtensionObject(Logger logger, MetricRegistry metricRegistry, ScriptContext scriptContext) {
        return new ShutdownHookPlugin(logger,metricRegistry,scriptContext);
    }
}
