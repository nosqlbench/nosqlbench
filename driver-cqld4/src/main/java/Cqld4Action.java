import io.nosqlbench.engine.api.activityapi.core.SyncAction;

public class Cqld4Action implements SyncAction {

    private final Cqld4Activity activity;

    public Cqld4Action(int slot, Cqld4Activity activity) {
        this.activity = activity;
    }

    @Override
    public int runCycle(long cycle) {


        return 0;
    }

}
