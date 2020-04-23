package io.nosqlbench.engine.core;

import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import org.graalvm.polyglot.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.util.Map;

public class PolyglotScenarioController {

    private static final Logger logger = LoggerFactory.getLogger("SCENARIO/POLYGLOT");

    private final ScenarioController controller;

    public PolyglotScenarioController(ScenarioController inner) {
        this.controller = inner;
    }

    // for graal polyglot interop
//    public synchronized void run(Value vtimeout, Value vspec) {
//        int timeout = vtimeout.asInt();
//        run(timeout, vspec);
//    }
    public synchronized void run(Value v) {
        run(Integer.MAX_VALUE, v);
    }

    public synchronized void run(int timeout, Value spec) {
        logger.debug("run(Value) called with:" + spec);
        if (spec.isHostObject()) {
            controller.run(timeout, (ActivityDef) spec.asHostObject());
        } else if (spec.isString()) {
            controller.run(timeout, spec.asString());
        } else if (spec.hasMembers()) {
            controller.run(timeout, spec.as(Map.class));
        } else if (spec.isHostObject()) {
            Object o = spec.asHostObject();
            if (o instanceof ActivityDef) {
                controller.run(timeout, (ActivityDef) o);
            } else {
                throw new RuntimeException("unrecognized polyglot host object type for run: " + spec.toString());
            }
        } else {
            throw new RuntimeException("unrecognized polyglot base type for run: " + spec.toString());
        }
    }

    public synchronized void start(Value spec) {
        if (spec.isHostObject()) {
            controller.start((ActivityDef) spec.asHostObject());
        } else if (spec.isString()) {
            controller.start(spec.asString());
        } else if (spec.hasMembers()) {
            controller.start(spec.as(Map.class));
        } else {
            throw new RuntimeException("unknown base type for graal polyglot: " + spec.toString());
        }
    }

    public synchronized void stop(Value spec) {
        if (spec.isHostObject()) {
            controller.stop((ActivityDef) spec.asHostObject());
        } else if (spec.isString()) {
            controller.stop(spec.asString());
        } else if (spec.hasMembers()) {
            controller.stop(spec.as(Map.class));
        } else {
            throw new RuntimeException("unknown base type for graal polyglot: " + spec.toString());
        }
    }

    public synchronized void apply(Value spec) {
        Map<String, String> map = spec.as(Map.class);
        controller.apply(map);
    }

    public synchronized void await(Value spec) {
        awaitActivity(spec);
    }
    public synchronized void awaitActivity(Value spec) {
        if (spec.isHostObject()) {
            controller.await((ActivityDef) spec.asHostObject());
        } else if (spec.hasMembers()) {
            controller.await(spec.as(Map.class));
        } else if (spec.isString()) {
            controller.await(spec.asString());
        } else {
            throw new RuntimeException("unable to map type for await from polyglot value: " + spec);
        }
    }

    public synchronized void waitMillis(Value spec) {
        if (spec.fitsInLong()) {
            controller.waitMillis(spec.asLong());
        } else {
            throw new InvalidParameterException("long type can't contain " + spec.toString());
        }
    }

    public synchronized boolean isRunningActivity(Value spec) {
        if (spec.isHostObject()) {
            return controller.isRunningActivity((ActivityDef) spec.asHostObject());
        } else if (spec.isString()) {
            return controller.isRunningActivity(spec.asString());
        } else if (spec.hasMembers()) {
            return controller.isRunningActivity(spec.as(Map.class));
        } else {
            throw new InvalidParameterException("unable to map type for isRunningActivity from polyglot value: " + spec);
        }
    }

}
