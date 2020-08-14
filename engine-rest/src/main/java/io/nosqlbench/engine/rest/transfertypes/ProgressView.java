package io.nosqlbench.engine.rest.transfertypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.nosqlbench.engine.api.activityapi.core.ProgressMeter;

public class ProgressView {

    private final ProgressMeter progressMeter;

    public ProgressView(ProgressMeter progressMeter) {
        this.progressMeter = progressMeter;

    }

    @JsonProperty("details")
    public String getProgressDetails() {
        return progressMeter.getProgressDetails();
    }

    @JsonProperty("name")
    public String getName() {
        return progressMeter.getProgressName();
    }

    @JsonProperty("state")
    public String getState() {
        return progressMeter.getProgressState().toString();
    }

    @JsonProperty("completed")
    public double getProgress() {
        return progressMeter.getProgress();
    }


}
