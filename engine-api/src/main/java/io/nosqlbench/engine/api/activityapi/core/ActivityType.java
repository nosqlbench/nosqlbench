/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.engine.api.activityapi.core;

import io.nosqlbench.engine.api.activityapi.output.OutputDispenser;
import io.nosqlbench.engine.api.activityimpl.CoreServices;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.engine.api.activityimpl.action.CoreActionDispenser;
import io.nosqlbench.engine.api.activityimpl.motor.CoreMotorDispenser;
import io.nosqlbench.api.config.NBLabeledElement;
import io.nosqlbench.engine.api.activityapi.input.InputDispenser;
import io.nosqlbench.api.engine.activityimpl.ActivityDef;

import java.util.Map;
import java.util.Optional;

/**
 * <p>An ActivityType is the central extension point in NB for new
 * activity types drivers. It is responsible for naming the activity type, as well as providing
 * the input, activity, and motor instances that will be assembled into an activity.</p>
 * <p>At the very minimum, a useful implementation of an activity type should provide
 * an action dispenser. Default implementations of input and motor dispensers are provided,
 * and by extension, default inputs and motors.</p>
 */
@Deprecated(forRemoval = true,since = "5.0")
public interface ActivityType<A extends Activity> {


    /**
     * Create an instance of an activity from the activity type.
     *
     * @param activityDef the definition that initializes and controls the activity.
     * @return a distinct Activity instance for each call
     */
    @SuppressWarnings("unchecked")
    default A getActivity(final ActivityDef activityDef, final NBLabeledElement parentLabels) {
        final SimpleActivity activity = new SimpleActivity(activityDef, parentLabels);
        return (A) activity;
    }

    /**
     * Create an instance of an activity that ties together all the components into a usable
     * activity instance. This is the method that should be called by executor classes.
     *
     * @param activityDef the definition that initializez and controlls the activity.
     * @param activities  a map of existing activities
     * @return a distinct activity instance for each call
     */
    default Activity getAssembledActivity(final ActivityDef activityDef, final Map<String, Activity> activities, final NBLabeledElement labels) {
        final A activity = this.getActivity(activityDef, labels);

        final InputDispenser inputDispenser = this.getInputDispenser(activity);
        if (inputDispenser instanceof ActivitiesAware) ((ActivitiesAware) inputDispenser).setActivitiesMap(activities);
        activity.setInputDispenserDelegate(inputDispenser);

        final ActionDispenser actionDispenser = this.getActionDispenser(activity);
        if (actionDispenser instanceof ActivitiesAware)
            ((ActivitiesAware) actionDispenser).setActivitiesMap(activities);
        activity.setActionDispenserDelegate(actionDispenser);

        final OutputDispenser outputDispenser = this.getOutputDispenser(activity).orElse(null);
        if ((null != outputDispenser) && (outputDispenser instanceof ActivitiesAware))
            ((ActivitiesAware) outputDispenser).setActivitiesMap(activities);
        activity.setOutputDispenserDelegate(outputDispenser);

        final MotorDispenser motorDispenser = this.getMotorDispenser(activity, inputDispenser, actionDispenser, outputDispenser);
        if (motorDispenser instanceof ActivitiesAware) ((ActivitiesAware) motorDispenser).setActivitiesMap(activities);
        activity.setMotorDispenserDelegate(motorDispenser);

        return activity;
    }

    /**
     * This method will be called <em>once</em> per action instance.
     *
     * @param activity The activity instance that will parameterize the returned MarkerDispenser instance.
     * @return an instance of MarkerDispenser
     */
    default Optional<OutputDispenser> getOutputDispenser(final A activity) {
        return CoreServices.getOutputDispenser(activity);
    }

    /**
     * This method will be called <em>once</em> per action instance.
     *
     * @param activity The activity instance that will parameterize the returned ActionDispenser instance.
     * @return an instance of ActionDispenser
     */
    default ActionDispenser getActionDispenser(final A activity) {
        return new CoreActionDispenser(activity);
    }

    /**
     * Return the InputDispenser instance that will be used by the associated activity to create Input factories
     * for each thread slot.
     *
     * @param activity the Activity instance which will parameterize this InputDispenser
     * @return the InputDispenser for the associated activity
     */
    default InputDispenser getInputDispenser(final A activity) {
        return CoreServices.getInputDispenser(activity);
    }

    default <T> MotorDispenser<T> getMotorDispenser(
        final A activity,
        final InputDispenser inputDispenser,
        final ActionDispenser actionDispenser,
        final OutputDispenser outputDispenser) {
        return new CoreMotorDispenser<T>(activity, inputDispenser, actionDispenser, outputDispenser);
    }

    /**
     * An ActivityType can describe the canonical named types as known within that driver implementation,
     * and the Java compatible types which can be assigned to them. This map is consulted when users need to select
     * the name from within that driver in order to see the compatible functions which may produce a valid type for
     * it. For example, a CQL users may want to know what java type (and thus what binding functions) can support
     * the CQL timeuuid type.
     *
     * Conversely, a user may want to know what types are supported by the java.util.{@link java.util.UUID} class
     * in CQL. In that case, this map will also be consulted, and multiple keys will match.
     * When there are multiple answers in this way, but the driver has its own default about which one to use
     * when the user's intent is ambiguous, the map should be be ordered as in {@link java.util.LinkedHashMap},
     * and the preferred form should be listed first.
     *
     * @return a type map indicating co-compatible associations of driver-specific type names and Java types
     */
    default Map<String,Class<?>> getTypeMap() {
        return Map.of();
    }

}
