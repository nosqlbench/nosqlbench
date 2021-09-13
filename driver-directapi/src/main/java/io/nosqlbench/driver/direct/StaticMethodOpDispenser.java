package io.nosqlbench.driver.direct;

import io.nosqlbench.engine.api.activityimpl.OpDispenser;

import java.lang.reflect.Method;
import java.util.function.LongFunction;

public class StaticMethodOpDispenser implements OpDispenser<DirectCall> {
    private final LongFunction<Object[]> argsfunc;
    private final Method method;
    private final Object instance;

    public StaticMethodOpDispenser(Method method, Object instance, LongFunction<Object[]> argsfunc) {
        this.method = method;
        this.instance = instance;
        this.argsfunc = argsfunc;
    }

    @Override
    public DirectCall apply(long value) {
        Object[] args = argsfunc.apply(value);
        return new DirectCall(method, instance, args);
    }
}
