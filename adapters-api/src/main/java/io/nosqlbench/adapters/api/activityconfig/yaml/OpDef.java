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

package io.nosqlbench.adapters.api.activityconfig.yaml;

import io.nosqlbench.adapters.api.activityconfig.MultiMapLookup;
import io.nosqlbench.adapters.api.activityconfig.rawyaml.RawOpDef;
import io.nosqlbench.api.errors.BasicError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class OpDef extends OpTemplate {
    private final static Logger logger = LogManager.getLogger(OpDef.class);

    private final RawOpDef rawOpDef;
    private final OpsBlock block;
    private final LinkedHashMap<String, Object> params;
    private final LinkedHashMap<String, String> bindings;
    private final LinkedHashMap<String, String> tags;

    public OpDef(OpsBlock block, RawOpDef rawOpDef) {
        this.block = block;
        this.rawOpDef = rawOpDef;
        this.params = composeParams();
        this.bindings = composeBindings();
        this.tags = composeTags();
    }

    @Override
    public String getName() {
        return block.getName() + "--" + rawOpDef.getName();
    }

    @Override
    public Optional<Map<String, Object>> getOp() {
        Object op = rawOpDef.getOp();
        if (op == null) {
            return Optional.empty();
        }
        HashMap<String, Object> newmap = new LinkedHashMap<>();
        if (op instanceof Map) {
            ((Map<?, ?>) op).forEach((k, v) -> {
                newmap.put(k.toString(), v);
            });
        } else if (op instanceof CharSequence) {
            newmap.put("stmt", op.toString());
        } else if (op instanceof List list) {
            newmap.put("stmt", list);
        } else {
            throw new BasicError("Unable to coerce a '" + op.getClass().getCanonicalName() + "' into an op template");
        }

        return Optional.of(newmap);
    }

    @Override
    public LinkedHashMap<String, String> getBindings() {
        return bindings;
//        return new MultiMapLookup<>(rawStmtDef.getBindings(), block.getBindings());
    }

    private LinkedHashMap<String, String> composeBindings() {
        MultiMapLookup<String> lookup = new MultiMapLookup<>(rawOpDef.getBindings(), block.getBindings());
        return new LinkedHashMap<>(lookup);
    }

    @Override
    public Map<String, Object> getParams() {
        return params;
    }

    private LinkedHashMap<String, Object> composeParams() {
        MultiMapLookup<Object> lookup = new MultiMapLookup<>(rawOpDef.getParams(), block.getParams());
        LinkedHashMap<String, Object> params = new LinkedHashMap<>(lookup);
        return params;
    }



    @Override
    public Map<String, String> getTags() {
        return tags;
    }

    private LinkedHashMap<String, String> composeTags() {
        LinkedHashMap<String, String> tagsWithName = new LinkedHashMap<>(new MultiMapLookup<>(rawOpDef.getTags(), block.getTags()));
        tagsWithName.put("name",getName());
        tagsWithName.put("block",block.getName());
        return tagsWithName;
    }

    @Override
    public String toString() {
        return "stmt(name:" + getName() + ", stmt:" + getOp() + ", tags:(" + getTags() + "), params:(" + getParams() + "), bindings:(" + getBindings() + "))";
    }

    @Override
    public String getDesc() {
        return rawOpDef.getDesc();
    }

}
