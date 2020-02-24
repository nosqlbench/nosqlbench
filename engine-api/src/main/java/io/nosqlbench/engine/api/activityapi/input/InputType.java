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

package io.nosqlbench.engine.api.activityapi.input;

import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.util.Named;
import io.nosqlbench.engine.api.util.SimpleServiceLoader;

public interface InputType extends Named {

    public static SimpleServiceLoader<InputType> FINDER =
            new SimpleServiceLoader<>(InputType.class);

    InputDispenser getInputDispenser(Activity activity);
}
