/*
 * Copyright (c) 2023 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.nb.api.components.core;

import io.nosqlbench.nb.api.components.decorators.NBTokenWords;
import io.nosqlbench.nb.api.components.events.ComponentOutOfScope;
import io.nosqlbench.nb.api.components.events.DownEvent;
import io.nosqlbench.nb.api.components.events.NBEvent;
import io.nosqlbench.nb.api.components.events.UpEvent;
import io.nosqlbench.nb.api.engine.metrics.MetricsCloseable;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetric;
import io.nosqlbench.nb.api.labels.NBLabels;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class NBBaseComponent extends NBBaseComponentMetrics implements NBComponent, NBTokenWords, NBComponentTimeline {
    private final static Logger logger = LogManager.getLogger("RUNTIME");
    protected final NBComponent parent;
    protected final NBLabels labels;
    private final List<NBComponent> children = new ArrayList<>();
    protected NBMetricsBuffer metricsBuffer = new NBMetricsBuffer();
    protected boolean bufferOrphanedMetrics = false;
    private ConcurrentHashMap<String,String> props = new ConcurrentHashMap<>();
    protected Exception error;
    protected long started_ns, teardown_ns, closed_ns, errored_ns, started_epoch_ms;
    protected NBInvokableState state = NBInvokableState.STARTING;
    private static final List<MetricsCloseable> metricsCloseables = new ArrayList<>();

    public NBBaseComponent(NBComponent parentComponent) {
        this(parentComponent, NBLabels.forKV());
    }

    public NBBaseComponent(NBComponent parentComponent, NBLabels componentSpecificLabelsOnly) {
        this.started_ns = System.nanoTime();
        this.started_epoch_ms = System.currentTimeMillis();
        this.labels = componentSpecificLabelsOnly;
        if (parentComponent != null) {
            parent = parentComponent;
            parent.attachChild(this);
        } else {
            parent = null;
        }
        state = (state==NBInvokableState.ERRORED) ? state : NBInvokableState.RUNNING;
    }

    public NBBaseComponent(NBComponent parentComponent, NBLabels componentSpecificLabelsOnly, Map<String, String> props) {
        this(parentComponent,componentSpecificLabelsOnly);
        props.forEach(this::setComponentProp);
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
                NBLabels eachLabels = extant.getComponentOnlyLabels();
                NBLabels newLabels = child.getComponentOnlyLabels();

                if (eachLabels!=null && newLabels!=null && !eachLabels.isEmpty() && !newLabels.isEmpty() && child.getComponentOnlyLabels().equals(extant.getComponentOnlyLabels())) {
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
        state = (state==NBInvokableState.ERRORED) ? state : NBInvokableState.CLOSING;
        closed_ns = System.nanoTime();

        try {
            logger.debug("cleaning up");
            ArrayList<NBComponent> children = new ArrayList<>(getChildren());
            for (NBComponent child : children) {
                child.close();
            }
            for (MetricsCloseable metricsCloseable : metricsCloseables) {
                metricsCloseable.closeMetrics();
            }
        } catch (Exception e) {
            onError(e);
        } finally {
            logger.debug("detaching " + description());
            if (parent != null) {
                parent.detachChild(this);
            }
            teardown();
        }
    }

    public void onError(Exception e) {
        RuntimeException wrapped = new RuntimeException("While in state " + this.state + ", an error occured: " + e, e);
        logger.error(wrapped);
        this.error = wrapped;
        state=NBInvokableState.ERRORED;
    }
    /**
     * Override this method in your component implementations when you need to do something
     * to close out your component.
     */
    protected void teardown() {
        logger.debug("tearing down " + description());
        this.teardown_ns = System.nanoTime();
        this.state=(state==NBInvokableState.ERRORED) ? state : NBInvokableState.STOPPED;
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
            case ComponentOutOfScope coos -> {
                for (NBMetric m : this.getComponentMetrics()) {
                    reportExecutionMetric(m);
                }
                if (bufferOrphanedMetrics) {
                    metricsBuffer.printMetricSummary(this);
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

    /**
     * This method is called by the engine to report a component going out of scope. The metrics for that component
     * will bubble up through the component layers and can be buffered for reporting at multiple levels.
     *
     * @param m The metric to report
     */
    @Override
    public void reportExecutionMetric(NBMetric m) {
        if (bufferOrphanedMetrics) {
            metricsBuffer.addSummaryMetric(m);
        }
        if (parent != null) {
            parent.reportExecutionMetric(m);
        }
    }

    @Override
    public long getNanosSinceStart() {
        if (teardown_ns ==0) {
            return System.nanoTime()- started_ns;
        } else {
            return teardown_ns - started_ns;
        }
    }

    @Override
    public Optional<String> getComponentProp(String name) {
        if (this.props!=null && this.props.containsKey(name)) {
            return Optional.ofNullable(this.props.get(name));
        } else if (this.getParent()!=null) {
                return this.getParent().getComponentProp(name);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public NBComponentProps setComponentProp(String name, String value) {
        if (this.props==null) {
            this.props = new ConcurrentHashMap<>();
        }
        props.put(name, value);
        return this;
    }

    @Override
    public NBInvokableState getComponentState() {
        return state;
    }

    @Override
    public long nanosof_start() {
        return this.started_ns;
    }

    @Override
    public long nanosof_close() {
        return this.closed_ns;
    }

    @Override
    public long nanosof_teardown() {
        return this.teardown_ns;
    }

    @Override
    public long nanosof_error() {
        return this.errored_ns;
    }

    @Override
    public long started_epoch_ms() {
        return this.started_epoch_ms;
    }

    public void addMetricsCloseable(MetricsCloseable metric) {
        metricsCloseables.add(metric);
    }

}
