package io.nosqlbench.engine.api.activityimpl.uniform;

import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.MotorDispenser;
import io.nosqlbench.engine.api.activityapi.cyclelog.filters.IntPredicateDispenser;
import io.nosqlbench.engine.api.activityapi.input.InputDispenser;
import io.nosqlbench.engine.api.activityapi.output.OutputDispenser;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.nb.api.engine.activityimpl.ParameterMap;
import io.nosqlbench.nb.api.labels.NBLabels;

public class ActivityWiring {

    private final ActivityDef activityDef;
    private MotorDispenser<?> motorDispenser;
    private InputDispenser inputDispenser;
    private ActionDispenser actionDispenser;
    private OutputDispenser markerDispenser;
    private IntPredicateDispenser resultFilterDispenser;

    public ActivityWiring(ActivityDef activityDef) {
        this.activityDef = activityDef;
    }

    public static ActivityWiring of(ActivityDef activityDef) {
        return new ActivityWiring(activityDef);
    }

    public ActivityDef getActivityDef() {
        return activityDef;
    }

    public final MotorDispenser<?> getMotorDispenserDelegate() {
        return motorDispenser;
    }

    public final void setMotorDispenserDelegate(MotorDispenser<?> motorDispenser) {
        this.motorDispenser = motorDispenser;
    }

    public final InputDispenser getInputDispenserDelegate() {
        return inputDispenser;
    }

    public final void setInputDispenserDelegate(InputDispenser inputDispenser) {
        this.inputDispenser = inputDispenser;
    }

    public final ActionDispenser getActionDispenserDelegate() {
        return actionDispenser;
    }

    public final void setActionDispenserDelegate(ActionDispenser actionDispenser) {
        this.actionDispenser = actionDispenser;
    }

    public IntPredicateDispenser getResultFilterDispenserDelegate() {
        return resultFilterDispenser;
    }

    public void setResultFilterDispenserDelegate(IntPredicateDispenser resultFilterDispenser) {
        this.resultFilterDispenser = resultFilterDispenser;
    }

    public OutputDispenser getMarkerDispenserDelegate() {
        return this.markerDispenser;
    }

    public void setOutputDispenserDelegate(OutputDispenser outputDispenser) {
        this.markerDispenser = outputDispenser;
    }


    public ParameterMap getParams() {
        return activityDef.getParams();
    }
}
