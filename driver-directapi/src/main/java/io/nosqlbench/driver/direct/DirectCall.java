package io.nosqlbench.driver.direct;

import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.Op;

import java.lang.reflect.Method;

public class DirectCall implements Op,Runnable {
    private final Method method;
    private final Object[] args;
    private final Object instance;

    public DirectCall(Method method, Object instance, Object[] args) {
        this.method = method;
        this.instance = instance;
        this.args = args;
    }

    @Override
    public void run() {
        try {
            method.invoke(instance,args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
