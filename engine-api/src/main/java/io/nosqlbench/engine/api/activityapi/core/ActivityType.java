/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.engine.api.activityapi.core;

import io.nosqlbench.engine.api.activityapi.input.InputDispenser;
import io.nosqlbench.engine.api.activityapi.output.OutputDispenser;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.CoreServices;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.engine.api.activityimpl.action.CoreActionDispenser;
import io.nosqlbench.engine.api.activityimpl.motor.CoreMotorDispenser;
import io.nosqlbench.engine.api.util.Named;
import io.nosqlbench.engine.api.util.SimpleServiceLoader;

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
public interface ActivityType<A extends Activity> extends Named {

    public static SimpleServiceLoader<ActivityType> FINDER = new SimpleServiceLoader<>(ActivityType.class);
    /**
     * Return the short name of this activity type. The fully qualified name of an activity type is
     * this value, prefixed by the package of the implementing class.
     *
     * @return An activity type name, like "diag"
     */
    String getName();

    /**
     * Create an instance of an activity from the activity type.
     *
     * @param activityDef the definition that initializes and controls the activity.
     * @return a distinct Activity instance for each call
     */
    @SuppressWarnings("unchecked")
    default A getActivity(ActivityDef activityDef) {
        SimpleActivity activity = new SimpleActivity(activityDef);
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
    default Activity getAssembledActivity(ActivityDef activityDef, Map<String, Activity> activities) {
        A activity = getActivity(activityDef);

        InputDispenser inputDispenser = getInputDispenser(activity);
        if (inputDispenser instanceof ActivitiesAware) {
            ((ActivitiesAware) inputDispenser).setActivitiesMap(activities);
        }
        activity.setInputDispenserDelegate(inputDispenser);

        ActionDispenser actionDispenser = getActionDispenser(activity);
        if (actionDispenser instanceof ActivitiesAware) {
            ((ActivitiesAware) actionDispenser).setActivitiesMap(activities);
        }
        activity.setActionDispenserDelegate(actionDispenser);

        OutputDispenser outputDispenser = getOutputDispenser(activity).orElse(null);
        if (outputDispenser !=null && outputDispenser instanceof ActivitiesAware) {
            ((ActivitiesAware) outputDispenser).setActivitiesMap(activities);
        }
        activity.setOutputDispenserDelegate(outputDispenser);

        MotorDispenser motorDispenser = getMotorDispenser(activity, inputDispenser, actionDispenser, outputDispenser);
        if (motorDispenser instanceof ActivitiesAware) {
            ((ActivitiesAware) motorDispenser).setActivitiesMap(activities);
        }
        activity.setMotorDispenserDelegate(motorDispenser);

        return activity;
    }

    /**
     * This method will be called <em>once</em> per action instance.
     *
     * @param activity The activity instance that will parameterize the returned MarkerDispenser instance.
     * @return an instance of MarkerDispenser
     */
    default Optional<OutputDispenser> getOutputDispenser(A activity) {
        return CoreServices.getOutputDispenser(activity);
    }

    /**
     * This method will be called <em>once</em> per action instance.
     *
     * @param activity The activity instance that will parameterize the returned ActionDispenser instance.
     * @return an instance of ActionDispenser
     */
    default ActionDispenser getActionDispenser(A activity) {
        return new CoreActionDispenser(activity);
    }

    /**
     * Return the InputDispenser instance that will be used by the associated activity to create Input factories
     * for each thread slot.
     *
     * @param activity the Activity instance which will parameterize this InputDispenser
     * @return the InputDispenser for the associated activity
     */
    default InputDispenser getInputDispenser(A activity) {
        return CoreServices.getInputDispenser(activity);
    }

    default <T> MotorDispenser<T> getMotorDispenser(
            A activity,
            InputDispenser inputDispenser,
            ActionDispenser actionDispenser,
            OutputDispenser outputDispenser) {
        return new CoreMotorDispenser<T> (activity, inputDispenser, actionDispenser, outputDispenser);
    }


}
