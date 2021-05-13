package io.nosqlbench.driver.cqld4;

import io.nosqlbench.engine.api.activityapi.errorhandling.modular.NBErrorHandler;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Function;

/**
 * @see <a href="https://docs.datastax.com/en/developer/java-driver/4.11/manual/core/integration/">Cql driver 4 Integration</a>
 */
public class Cqld4Activity extends SimpleActivity {

    private final static Logger logger = LogManager.getLogger(Cqld4Activity.class);

    private OpSequence<OpDispenser<Cqld4Op>> sequence;
    private NBErrorHandler errorhandler;

    public Cqld4Activity(ActivityDef activityDef) {
        super(activityDef);
    }

    @Override
    public void initActivity() {
        super.initActivity();
        Function<OpTemplate,OpDispenser<Cqld4Op>> f = Cqld4ReadyOp::new;
        sequence = createOpSequence(f);
        this.errorhandler = new NBErrorHandler(
            () -> activityDef.getParams().getOptionalString("errors").orElse("stop"),
            this::getExceptionMetrics
        );

    }

    public NBErrorHandler getErrorhandler() {
        return errorhandler;
    }

    public OpSequence<OpDispenser<Cqld4Op>> getSequence() {
        return sequence;
    }

    public int getMaxTries() {
        return 0;
    }
}
