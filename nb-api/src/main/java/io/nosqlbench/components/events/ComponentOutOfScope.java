package io.nosqlbench.components.events;

import io.nosqlbench.components.DownEvent;
import io.nosqlbench.components.NBComponent;

public class ComponentOutOfScope implements NBEvent {
    private final NBComponent component;

    public ComponentOutOfScope(NBComponent component) {
        this.component = component;
    }
}
