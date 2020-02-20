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
package io.nosqlbench.activityimpl.action;

import io.nosqlbench.activityapi.core.Action;
import io.nosqlbench.activityapi.core.ActionDispenser;
import io.nosqlbench.activityapi.core.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An action dispenser that returns a logging action.
 */
public class CoreActionDispenser implements ActionDispenser {

    private final static Logger logger = LoggerFactory.getLogger(CoreActionDispenser.class);

    private Activity activity;

    public CoreActionDispenser(Activity activity) {
        this.activity = activity;
    }

    @Override
    public Action getAction(int slot) {
        return new CoreAction(activity.getActivityDef(), slot);
    }
}
