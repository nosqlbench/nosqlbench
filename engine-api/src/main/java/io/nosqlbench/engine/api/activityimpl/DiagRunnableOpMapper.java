/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.engine.api.activityimpl;

import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.templating.CommandTemplate;

import java.util.function.Function;

public class DiagRunnableOpMapper implements Function<OpTemplate, OpDispenser<Runnable>> {

    @Override
    public OpDispenser<Runnable> apply(OpTemplate optpl) {
        CommandTemplate commandTemplate = new CommandTemplate(optpl);
        return new DiagRunnableOpDispenser(commandTemplate);
    }

}
