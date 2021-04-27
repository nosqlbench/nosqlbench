package io.nosqlbench.engine.api.activityapi.errorhandling.modular;

import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.config.ConfigAware;
import io.nosqlbench.nb.api.config.ConfigModel;
import io.nosqlbench.nb.api.config.MutableConfigModel;

import java.util.Map;

@Service(value = ErrorHandler.class, selector = "code")
public class ResultCode implements ErrorHandler, ConfigAware {

    private byte code;

    @Override
    public ErrorDetail handleError(String name, Throwable t, long cycle, long durationInNanos, ErrorDetail detail) {
        return detail.withResultCode(code);
    }

    @Override
    public void applyConfig(Map<String, ?> providedConfig) {
        this.code = Byte.valueOf(providedConfig.get("code").toString());
    }

    @Override
    public ConfigModel getConfigModel() {
        return new MutableConfigModel(this)
            .required("code", Byte.class)
            .asReadOnly();
    }
}
