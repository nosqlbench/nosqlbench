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

package io.nosqlbench.engine.api.activityimpl.uniform.decorators;

import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDocList;
import io.nosqlbench.engine.api.templating.DriverAdapterDecorators;

import java.util.List;
import java.util.Map;

/**
 * In the case that no statements are provided for an activity, but a workload
 * description is provided, and the result for no found statements is not
 * simply a matter of tag filtering, an activity may provide its own
 * synthetic ops. This is here primarily to support the classic behavior of stdout
 * until it's functionality is subsumed by standard diagnostic features.
 *
 * Note that this is only valid while an activity uses a single driver, which will
 * change with upcoming API updates.
 */
public interface SyntheticOpTemplateProvider extends DriverAdapterDecorators {

    /**
     * If a driver adapter supports creating example op templates from bindings,
     * it must implement this method to do so.
     * @param stmtsDocList The existing doc structure, which should contain no fully defined op templates, but may contain other elements like bindings
     * @return A list of op templates, size zero or more
     */
    List<OpTemplate> getSyntheticOpTemplates(StmtsDocList stmtsDocList, Map<String,Object> params);
}
