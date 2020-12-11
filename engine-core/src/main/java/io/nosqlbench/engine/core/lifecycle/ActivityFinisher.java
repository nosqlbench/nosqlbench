package io.nosqlbench.engine.core.lifecycle;

public class ActivityFinisher extends Thread {

    private final ActivityExecutor executor;
    private final int timeout;
    private boolean result;

    public ActivityFinisher(ActivityExecutor executor, int timeout) {
        super(executor.getActivityDef().getAlias() + "_finisher");
        this.executor = executor;
        this.timeout = timeout;
    }

    @Override
    public void run() {
        result = executor.awaitCompletion(timeout);
    }

    public boolean getResult() {
        return result;
    }
}
