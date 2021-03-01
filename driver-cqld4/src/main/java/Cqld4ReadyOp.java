import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;

public class Cqld4ReadyOp implements OpDispenser<Cqld4Op> {

    public Cqld4ReadyOp(OpTemplate template) {
    }

    @Override
    public Cqld4Op apply(long value) {
        return null;
    }
}
