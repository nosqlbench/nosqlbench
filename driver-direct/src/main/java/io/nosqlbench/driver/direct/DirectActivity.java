package io.nosqlbench.driver.direct;

import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.uniform.StandardActivity;

import java.util.function.Function;

public class DirectActivity extends StandardActivity<DirectCall> implements Activity {

    public DirectActivity(ActivityDef activityDef) {
        super(activityDef);
    }

    @Override
    protected Function<OpTemplate, OpDispenser<DirectCall>> getOpMapperFunction() {
        return DirectOpMapper::new;
    }
}
