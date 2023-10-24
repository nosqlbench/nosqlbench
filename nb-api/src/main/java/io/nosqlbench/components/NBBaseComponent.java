package io.nosqlbench.components;

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


import io.nosqlbench.api.engine.metrics.instruments.NBMetric;
import io.nosqlbench.api.labels.NBLabels;
import io.nosqlbench.components.decorators.NBTokenWords;
import io.nosqlbench.components.events.NBEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class NBBaseComponent extends NBBaseComponentMetrics implements NBComponent, NBTokenWords {
    private final static Logger logger = LogManager.getLogger("RUNTIME");
    protected final NBComponent parent;
    protected final NBLabels labels;
    private final List<NBComponent> children = new ArrayList<>();

    public NBBaseComponent(NBComponent parentComponent) {
        this(parentComponent, NBLabels.forKV());
    }

    public NBBaseComponent(NBComponent parentComponent, NBLabels componentSpecificLabelsOnly) {
        this.labels = componentSpecificLabelsOnly;
        if (parentComponent != null) {
            parent = parentComponent;
            parent.attachChild(this);
        } else {
            parent = null;
        }
    }

    @Override
    public NBComponent getParent() {
        return parent;
    }

    @Override
    public NBComponent attachChild(NBComponent... children) {

        for (NBComponent child : children) {
            logger.debug(() -> "attaching " + child.description() + " to parent " + this.description());
            for (NBComponent extant : this.children) {
                if (!child.getComponentOnlyLabels().isEmpty() && child.getComponentOnlyLabels().equals(extant.getComponentOnlyLabels())) {
                    throw new RuntimeException("Adding second child under already-defined labels is not allowed:\n" +
                        " extant: (" + extant.getClass().getSimpleName() + ") " + extant.description() + "\n" +
                        " adding: (" + child.getClass().getSimpleName() + ") " + child.description());
                }
            }

            this.children.add(child);
        }
        return this;
    }

    @Override
    public NBComponent detachChild(NBComponent... children) {
        for (NBComponent child : children) {
            logger.debug(() -> "detaching " + child.description() + " from " + this.description());
            this.children.remove(child);
        }

        return this;
    }

    @Override
    public List<NBComponent> getChildren() {
        return children;
    }

    @Override
    public NBLabels getLabels() {
        NBLabels effectiveLabels = (this.parent == null ? NBLabels.forKV() : parent.getLabels());
        effectiveLabels = (this.labels == null) ? effectiveLabels : effectiveLabels.and(this.labels);
        return effectiveLabels;
    }

    @Override
    public NBLabels getComponentOnlyLabels() {
        return this.labels;
    }


    @Override
    public void beforeDetach() {
        logger.debug("before detach " + description());
    }

    @Override
    public final void close() throws RuntimeException {
        try {
            logger.debug("cleaning up");
            ArrayList<NBComponent> children = new ArrayList<>(getChildren());
            for (NBComponent child : children) {
                child.close();
            }
            teardown();
        } catch (Exception e) {
            logger.error(e);
        } finally {
            logger.debug("detaching " + description());
            if (parent != null) {
                parent.detachChild(this);
            }
        }
    }

    /**
     * Override this method in your component implementations when you need to do something
     * to close out your component.
     */
    protected void teardown() {
        logger.debug("tearing down " + description());
    }

    @Override
    public NBCreators create() {
        return new NBCreators(this);
    }

    @Override
    public NBFinders find() {
        return new NBFinders(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        if (!getComponentMetrics().isEmpty()) {
            sb.append(System.lineSeparator()).append("metrics:");
            for (NBMetric componentMetric : getComponentMetrics()) {
                sb.append(System.lineSeparator()).append("  ").append(componentMetric.toString());
            }
        }
        return sb.toString();
    }


    @Override
    public void onEvent(NBEvent event) {
        logger.debug(() -> description() + " handling event " + event.toString());
        switch (event) {
            case UpEvent ue -> {
                if (parent != null) parent.onEvent(ue);
            }
            case DownEvent de -> {
                for (NBComponent child : children) {
                    child.onEvent(de);
                }
            }
            default -> logger.warn("dropping event " + event);
        }
    }

    @Override
    public <T> Optional<T> findParentService(Class<T> type) {
        return findServiceOn(type, this);
    }

    private <T> Optional<T> findServiceOn(Class<T> type, NBComponent target) {
        if (type.isAssignableFrom(target.getClass())) {
            return Optional.of(type.cast(target));
        } else if (getParent() != null) {
            return findServiceOn(type, getParent());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Map<String, String> getTokens() {
        return getLabels().asMap();
    }

}
