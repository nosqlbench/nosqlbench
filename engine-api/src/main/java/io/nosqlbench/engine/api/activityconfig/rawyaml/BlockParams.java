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

package io.nosqlbench.engine.api.activityconfig.rawyaml;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class BlockParams extends Tags {

    private String name = "";
    private String desc = "";
    private Map<String, String> bindings = new LinkedHashMap<>();
    private Map<String, String> params = new LinkedHashMap<>();

    public BlockParams() {}

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
    public void setDescription(String desc) {
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getBindings() {
        return Collections.unmodifiableMap(bindings);
    }

    public void setBindings(Map<String, String> bindings) {
        this.bindings.clear();
        this.bindings.putAll(bindings);
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> config) {
        this.params.clear();
        this.params.putAll(config);
    }

    public void applyBlockParams(BlockParams other) {
        setName(other.getName());
        setBindings(other.getBindings());
        setTags(other.getTags());
        setParams(other.getParams());
    }
}
