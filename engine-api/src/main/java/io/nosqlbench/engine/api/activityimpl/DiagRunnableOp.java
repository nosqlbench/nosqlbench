package io.nosqlbench.engine.api.activityimpl;

public class DiagRunnableOp implements Runnable {

    public final String message;

    public DiagRunnableOp(String message) {
        this.message = message;
    }

    @Override
    public void run() {
    }

    @Override
    public String toString() {
        return message;
    }

}
