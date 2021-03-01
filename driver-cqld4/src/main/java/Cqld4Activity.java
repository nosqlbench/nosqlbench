import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Function;

public class Cqld4Activity extends SimpleActivity {

    private final static Logger logger = LogManager.getLogger(Cqld4Activity.class);

    private OpSequence<OpDispenser<Cqld4Op>> sequence;

    public Cqld4Activity(ActivityDef activityDef) {
        super(activityDef);
    }

    @Override
    public void initActivity() {
        super.initActivity();
        Function<OpTemplate,OpDispenser<Cqld4Op>> f = Cqld4ReadyOp::new;
        sequence = createOpSequence(f);
    }
}
