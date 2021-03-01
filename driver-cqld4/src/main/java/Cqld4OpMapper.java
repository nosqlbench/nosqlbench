import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;

import java.util.function.Function;

public class Cqld4OpMapper implements Function<OpTemplate, OpDispenser<Cqld4Op>> {

    public Cqld4OpMapper(OpTemplate opTemplate) {

    }

    @Override
    public OpDispenser<Cqld4Op> apply(OpTemplate opTemplate) {
        return null;
    }
}
