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

package io.nosqlbench.engine.api.activityimpl.uniform;


import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.core.ActivitiesAware;
import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.MotorDispenser;
import io.nosqlbench.engine.api.activityapi.input.InputDispenser;
import io.nosqlbench.engine.api.activityapi.output.OutputDispenser;
import io.nosqlbench.engine.api.activityimpl.CoreServices;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.engine.api.activityimpl.action.CoreActionDispenser;
import io.nosqlbench.engine.api.activityimpl.motor.CoreMotorDispenser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class StandardActivityType<A extends StandardActivity<?,?>> {

    private static final Logger logger = LogManager.getLogger("ACTIVITY");
    private final Map<String, DriverAdapter> adapters = new HashMap<>();
    private final NBComponent parent;
//    private final DriverAdapter<?, ?> adapter;
    private final ActivityDef activityDef;

    public StandardActivityType(final DriverAdapter<?,?> adapter, final ActivityDef activityDef, final NBComponent parent) {
        this.parent = parent;
//        this.adapter = adapter;
        this.activityDef = activityDef;
//        super(parent,activityDef
//            .deprecate("type","driver")
//            .deprecate("yaml", "workload")
//        );
        adapters.put(adapter.getAdapterName(),adapter);
        if (adapter instanceof ActivityDefAware) ((ActivityDefAware) adapter).setActivityDef(activityDef);
    }

    public StandardActivityType(final ActivityDef activityDef, final NBComponent parent) {
        this.parent = parent;
        this.activityDef = activityDef;

//        super(parent,activityDef);
    }

    /**
     * Create an instance of an activity from the activity type.
     *
     * @param activityDef the definition that initializes and controls the activity.
     * @return a distinct Activity instance for each call
     */
    @SuppressWarnings("unchecked")
    public A getActivity(final ActivityDef activityDef, final NBComponent parent) {
        if (activityDef.getParams().getOptionalString("async").isPresent())
            throw new RuntimeException("This driver does not support async mode yet.");

        return (A) new StandardActivity(parent, activityDef);
    }

    /**
     * This method will be called <em>once</em> per action instance.
     *
     * @param activity The activity instance that will parameterize the returned ActionDispenser instance.
     * @return an instance of ActionDispenser
     */
    public ActionDispenser getActionDispenser(final A activity) {
        return new StandardActionDispenser(activity);
    }

    /**
     * Create an instance of an activity that ties together all the components into a usable
     * activity instance. This is the method that should be called by executor classes.
     *
     * @param activityDef the definition that initializez and controlls the activity.
     * @param activities  a map of existing activities
     * @return a distinct activity instance for each call
     */
    public Activity getAssembledActivity(final ActivityDef activityDef, final Map<String, Activity> activities, final NBComponent parent) {
        final A activity = this.getActivity(activityDef, parent);

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
    public Optional<OutputDispenser> getOutputDispenser(final A activity) {
        return CoreServices.getOutputDispenser(activity);
    }

    /**
     * Return the InputDispenser instance that will be used by the associated activity to create Input factories
     * for each thread slot.
     *
     * @param activity the Activity instance which will parameterize this InputDispenser
     * @return the InputDispenser for the associated activity
     */
    public InputDispenser getInputDispenser(final A activity) {
        return CoreServices.getInputDispenser(activity);
    }

    public <T> MotorDispenser<T> getMotorDispenser(
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
    public Map<String,Class<?>> getTypeMap() {
        return Map.of();
    }

}
