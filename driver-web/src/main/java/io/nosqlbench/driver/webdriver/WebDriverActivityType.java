package io.nosqlbench.driver.webdriver;

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


import io.nosqlbench.engine.api.activityapi.core.Action;
import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.nb.annotations.Service;

@Service(value = ActivityType.class, selector = "webdriver")
public class WebDriverActivityType implements ActivityType<WebDriverActivity> {

    @Override
    public WebDriverActivity getActivity(ActivityDef activityDef) {
        return new WebDriverActivity(activityDef);
    }

    @Override
    public ActionDispenser getActionDispenser(WebDriverActivity activity) {
        return new WebDriverActionDispenser(activity);
    }

    private static class WebDriverActionDispenser implements ActionDispenser {

        private final Activity activity;

        private WebDriverActionDispenser(Activity activity) {
            this.activity = activity;
        }

        @Override
        public Action getAction(int slot) {
            return new WebDriverAction((WebDriverActivity) activity, slot);
        }
    }
}
