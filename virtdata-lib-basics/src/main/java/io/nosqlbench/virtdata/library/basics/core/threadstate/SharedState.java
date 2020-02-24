package io.nosqlbench.virtdata.library.basics.core.threadstate;

import java.util.*;

/**
 * This provides common thread local instancing for sharing a thread local map across classes.
 */
public class SharedState {

    // A thread-local map of objects by name
    public static ThreadLocal<HashMap<String,Object>> tl_ObjectMap = ThreadLocal.withInitial(HashMap::new);

    // A thread-local stack of objects by name
    public static ThreadLocal<Deque<Object>> tl_ObjectStack = ThreadLocal.withInitial(ArrayDeque::new);

    // A global map of objects for constant pool, etc.
    public static Map<String,Object> gl_ObjectMap = new HashMap<>();

}
