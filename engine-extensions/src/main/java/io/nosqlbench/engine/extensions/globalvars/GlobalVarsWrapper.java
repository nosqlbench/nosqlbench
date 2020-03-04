package io.nosqlbench.engine.extensions.globalvars;

import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;

import java.util.concurrent.ConcurrentHashMap;

public class GlobalVarsWrapper {
    ConcurrentHashMap<String, Object> map = SharedState.gl_ObjectMap;

    String test = "puppies";

    public ConcurrentHashMap<String, Object> getMap(){
        this.map.get("");
        return this.map;
    }

    public  String getTest(){
        return this.test;
    }
}
