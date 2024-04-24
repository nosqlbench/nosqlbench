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

package io.nosqlbench.adapters.api.activityconfig.rawyaml;

import java.util.Map;

/**
 * A StmtsDef contains a list of rawStmts, as well as all of the optional
 * block parameters that can be assigned to {@link RawOpFields}, which includes
 * a name, config values, data bindings, and filtering tags.
 */
public class RawOpsBlock extends OpsOwner {

    public RawOpsBlock() {}

    public void setFieldsByReflection(Map<String, Object> propsmap) {
        super.setFieldsByReflection(propsmap);
    }

}
