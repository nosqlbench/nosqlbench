package io.nosqlbench.engine.rest.transfertypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.nosqlbench.engine.api.activityapi.core.ProgressMeter;
import io.nosqlbench.engine.api.activityimpl.input.StateCapable;

public class ProgressView {

    private final ProgressMeter progressMeter;

    public ProgressView(ProgressMeter progressMeter) {
        if (progressMeter==null) {
            throw new RuntimeException("Unable to create a view with a null progressMeter");
        }
        this.progressMeter = progressMeter;
    }

    @JsonProperty("summary")
    public String getProgressDetails() {
        return progressMeter.getProgressSummary();
    }

    @JsonProperty("min")
    public long getMin() {
        return progressMeter.getProgressMin();
    }

    @JsonProperty("current")
    public long getCurrent() {
        return progressMeter.getProgressCurrent();
    }

    @JsonProperty("max")
    public long getMax() {
        return progressMeter.getProgressMax();
    }


    @JsonProperty("recycles_max")
    public long getRecyclesMax() {
        return progressMeter.getRecyclesMax();
    }

    @JsonProperty("recycles_current")
    public long getRecyclesCurrent() {
        return progressMeter.getRecyclesCurrent();
    }

    @JsonProperty("eta_millis")
    public double getEtaMills() {
        return progressMeter.getProgressETAMillis();
    }

    @JsonProperty("name")
    public String getName() {
        return progressMeter.getProgressName();
    }

    @JsonProperty("completed")
    public double getProgress() {
        return progressMeter.getProgressRatio();
    }

    @JsonProperty("state")
    public String getState() {
        if (progressMeter instanceof StateCapable) {
            return ((StateCapable)progressMeter).getRunState().toString();
        } else {
            return "unknown";
        }
    }


}
