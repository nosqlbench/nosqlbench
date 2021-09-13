package io.nosqlbench.engine.extensions.csvoutput;

import com.codahale.metrics.MetricRegistry;
import io.nosqlbench.engine.api.extensions.ScriptingPluginInfo;
import io.nosqlbench.nb.annotations.Service;
import org.apache.logging.log4j.Logger;

import javax.script.ScriptContext;

@Service(value = ScriptingPluginInfo.class,selector = "csvoutput")
public class CsvOutputPluginData implements ScriptingPluginInfo<CsvOutputPluginInstance> {

    @Override
    public String getDescription() {
        return "Write CSV output to a named file";
    }

    @Override
    public CsvOutputPluginInstance getExtensionObject(Logger logger, MetricRegistry metricRegistry, ScriptContext scriptContext) {
        return new CsvOutputPluginInstance();
    }
}
