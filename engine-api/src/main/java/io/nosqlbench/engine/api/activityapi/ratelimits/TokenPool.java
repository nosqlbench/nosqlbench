package io.nosqlbench.engine.api.activityapi.ratelimits;

public interface TokenPool {
    TokenPool apply(RateSpec rateSpec);

    double getBurstRatio();

    long takeUpTo(long amt);

    long blockAndTake();

    long blockAndTake(long tokens);

    long getWaitTime();

    long getWaitPool();

    long getActivePool();

    RateSpec getRateSpec();

    long restart();

    void start();
}
