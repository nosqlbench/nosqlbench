package io.nosqlbench.engine.api.templating;

import io.nosqlbench.virtdata.core.bindings.Binder;

import java.util.HashMap;
import java.util.Map;

public class ResolvedCommand<T> {

    private final ParsedCommand command;
    private final int mapsize;
    private final Map<String, Object> statics;
    private final Map<String, Binder<?>> dynamics;

    public ResolvedCommand(ParsedCommand command) {
        this.command = command;
        this.statics = command.getStatics();
        this.dynamics = resolveDynamics(command);
        this.mapsize = command.getStatics().size() + command.getDynamics().size();
    }

    private Map<String, Binder<?>> resolveDynamics(ParsedCommand command) {
        command.getDynamics().forEach((k,v) -> {

        });

        return null;
    }

    public Map<String,Object> getCommand(long seed) {
        HashMap<String, Object> map = new HashMap<>(mapsize);
        map.putAll(statics);

        dynamics.forEach((k, v) -> {
            map.put(k, v.bind(seed));
        });
        return map;

    }


}
