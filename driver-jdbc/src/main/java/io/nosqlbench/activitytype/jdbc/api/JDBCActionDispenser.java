/*
 *    Copyright 2015-2022 nosqlbench
 *
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
 */

package io.nosqlbench.activitytype.jdbc.api;

import io.nosqlbench.activitytype.jdbc.impl.JDBCAction;
import io.nosqlbench.engine.api.activityapi.core.Action;
import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;

public class JDBCActionDispenser implements ActionDispenser {
    private final JDBCActivity activity;

    public JDBCActionDispenser(JDBCActivity a) {
        activity = a;
    }

    @Override
    public Action getAction(int slot) {
        return new JDBCAction(activity, slot);
    }
}
