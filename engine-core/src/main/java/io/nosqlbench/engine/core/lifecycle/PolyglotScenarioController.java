package io.nosqlbench.engine.core.lifecycle;

/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graalvm.polyglot.Value;

import java.security.InvalidParameterException;
import java.util.Map;

public class PolyglotScenarioController {

    private static final Logger logger = LogManager.getLogger("SCENARIO/POLYGLOT");

    private final ScenarioController controller;

    public PolyglotScenarioController(ScenarioController inner) {
        this.controller = inner;
    }

    // for graal polyglot interop
//    public synchronized void run(Value vtimeout, Value vspec) {
//        int timeout = vtimeout.asInt();
//        run(timeout, vspec);
//    }
//
//    public synchronized void run(Map<String,String> map) {
//        controller.run(map);
//    }
//
//    public synchronized void run(int timeout, Map<String,String> map) {
//        controller.run(timeout, map);
//    }
//

    public synchronized void run(Object o) {
        if (o instanceof Value) {
            runValue((Value) o);
        } else if (o instanceof Map) {
            controller.run((Map<String, String>) o);
        } else if (o instanceof String) {
            controller.run(o.toString());
        } else {
            throw new RuntimeException("Unrecognized type: " + o.getClass().getCanonicalName());
        }
    }

    public synchronized void run(int timeout, Object o) {
        if (o instanceof Value) {
            runValue(timeout, (Value) o);
        } else if (o instanceof Map) {
            controller.run(timeout, (Map<String, String>) o);
        } else if (o instanceof String) {
            controller.run(timeout, o.toString());
        } else {
            throw new RuntimeException("Uncrecognized type: " + o.getClass().getCanonicalName());
        }
    }

    private synchronized void runValue(Value v) {
        runValue(Integer.MAX_VALUE, v);
    }

    private synchronized void runValue(int timeout, Value spec) {
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


    public synchronized void start(Object o) {
        if (o instanceof Value) {
            startValue((Value) o);
        } else if (o instanceof Map) {
            controller.start((Map<String, String>) o);
        } else if (o instanceof String) {
            controller.start(o.toString());
        } else {
            throw new RuntimeException("unrecognized type " + o.getClass().getCanonicalName());
        }
    }

    private synchronized void startValue(Value spec) {
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

    public synchronized void stop(Object o) {
        if (o instanceof Value) {
            stopValue((Value) o);
        } else if (o instanceof Map) {
            controller.stop((Map<String, String>) o);
        } else if (o instanceof String) {
            controller.stop(o.toString());
        } else {
            throw new RuntimeException("unknown type " + o.getClass().getCanonicalName());
        }
    }

    private synchronized void stopValue(Value spec) {
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

    public synchronized void forceStop(Object o) {
        if (o instanceof Value) {
            forceStop((Value) o);
        } else if (o instanceof Map) {
            controller.forceStop((Map<String, String>) o);
        } else if (o instanceof String) {
            controller.forceStop(o.toString());
        } else {
            throw new RuntimeException("unknown type " + o.getClass().getCanonicalName());
        }
    }

    private synchronized void forceStopValue(Value spec) {
        if (spec.isHostObject()) {
            controller.forceStop((ActivityDef) spec.asHostObject());
        } else if (spec.isString()) {
            controller.forceStop(spec.asString());
        } else if (spec.hasMembers()) {
            controller.forceStop(spec.as(Map.class));
        } else {
            throw new RuntimeException("unknown base type for graal polyglot: " + spec.toString());
        }
    }

    public synchronized void apply(Object o) {
        if (o instanceof Value) {
            applyValue((Value) o);
        } else if (o instanceof Map) {
            controller.apply((Map<String, String>) o);
        } else {
            throw new RuntimeException("unknown type: " + o.getClass().getCanonicalName());
        }
    }

    private synchronized void applyValue(Value spec) {
        Map<String, String> map = spec.as(Map.class);
        controller.apply(map);
    }

    public synchronized void awaitActivity(Object o) {
        this.await(o);
    }
    public synchronized void await(Object o) {
        if (o instanceof String) {
            controller.await(o.toString());
        } else if (o instanceof Value) {
            awaitValue((Value) o);
        } else if (o instanceof Map) {
            controller.await((Map<String, String>) o);
        } else {
            throw new RuntimeException("unknown type: " + o.getClass().getCanonicalName());
        }
    }

    private synchronized void awaitValue(Value spec) {
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

    public synchronized void waitMillis(Object o) {
        if (o instanceof Value) {
            waitMillisValue((Value) o);
        } else if (o instanceof Integer) {
            controller.waitMillis((Integer) o);
        } else if (o instanceof Long) {
            controller.waitMillis((Long) o);
        } else if (o instanceof String) {
            controller.waitMillis(Long.parseLong((String)o));
        } else {
            throw new RuntimeException("unknown type: " + o.getClass().getCanonicalName());
        }
    }

    private synchronized void waitMillisValue(Value spec) {
        if (spec.isString()) {
            controller.waitMillis(Long.parseLong(spec.asString()));
        } else if (spec.isNumber()) {
            controller.waitMillis(spec.asLong());
        } else {
            throw new InvalidParameterException(
                "unable to convert polyglot type " + spec.toString() + " to a long for waitMillis");
        }
    }

    public synchronized boolean isRunningActivity(Object o) {
        if (o instanceof Value) {
            return isRunningActivityValue((Value) o);
        } else if (o instanceof String) {
            return controller.isRunningActivity(o.toString());
        } else if (o instanceof Map) {
            return controller.isRunningActivity((Map<String, String>) o);
        } else {
            throw new RuntimeException("unknown type:" + o.getClass().getCanonicalName());
        }
    }

    private synchronized boolean isRunningActivityValue(Value spec) {
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
