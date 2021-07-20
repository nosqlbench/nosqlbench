package io.nosqlbench.engine.api.activityapi.errorhandling.modular;

import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.config.standard.NBMapConfigurable;
import io.nosqlbench.nb.api.config.standard.ConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;

import java.util.Map;

@Service(value = ErrorHandler.class, selector = "code")
public class ResultCode implements ErrorHandler, NBMapConfigurable {

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
    public NBConfigModel getConfigModel() {
        return ConfigModel.of(this.getClass())
            .required("code", Byte.class)
            .asReadOnly();
    }
}
