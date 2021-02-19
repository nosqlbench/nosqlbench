package io.nosqlbench.engine.extensions.http;

import com.codahale.metrics.MetricRegistry;
import io.nosqlbench.engine.api.extensions.ScriptingPluginInfo;
import io.nosqlbench.nb.annotations.Service;
import org.apache.logging.log4j.Logger;

import javax.script.ScriptContext;

@Service(value = ScriptingPluginInfo.class, selector = "http")
public class HttpPluginData implements ScriptingPluginInfo<HttpPlugin> {

    @Override
    public String getDescription() {
        return "use http get and post in scripts";
    }

    @Override
    public HttpPlugin getExtensionObject(Logger logger, MetricRegistry metricRegistry, ScriptContext scriptContext) {
        return new HttpPlugin();
    }
}
