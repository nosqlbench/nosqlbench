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

package io.nosqlbench.engine.core.script;

import java.util.HashMap;
import java.util.Map;

public class ScriptParams extends HashMap<String,String> {
    public Map<String,String> withOverrides(Map<String,String> overrides) {
        HashMap<String,String> result = new HashMap<>();
        result.putAll(this);
        result.putAll(overrides);
        return result;
    }
    public Map<String,String> withDefaults(Map<String,String> defaults) {
        HashMap<String,String> result = new HashMap<>();
        result.putAll(defaults);
        result.putAll(this);
        return result;
    }

}
