package io.nosqlbench.engine.api.activityimpl.uniform;

import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.nb.api.spi.SimpleServiceLoader;

public class StandardActivityType<A extends StandardActivity<?>> implements ActivityType<A> {

    public static SimpleServiceLoader<DriverAdapter> FINDER = new SimpleServiceLoader<DriverAdapter>(DriverAdapter.class);
    private final DriverAdapter<?> adapter;

    public StandardActivityType(DriverAdapter<?> adapter) {
        this.adapter = adapter;
    }

    @Override
    public A getActivity(ActivityDef activityDef) {
        return (A) new StandardActivity(adapter,activityDef);
    }

    @Override
    public ActionDispenser getActionDispenser(A activity) {
        return new StandardActionDispenser(activity);
    }
}
