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

package io.nosqlbench.adapters.api.activityimpl.uniform.decorators;

import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpsDocList;
import io.nosqlbench.adapters.api.templating.DriverAdapterDecorators;

import java.util.List;
import java.util.Map;

/**
 * It is possible for a DriverAdapter to create op templates using partial information.
 * For example, the stdout driver can use only bindings to create CSV or JSON style data renderings.
 * This mechanism is only triggered when:
 * <OL>
 * <LI>No op templates were provided.</LI>
 * <LI>The default driver for an activity implements this method.</LI>
 * </OL>
 * <p>
 * This excludes cases where a workload was provided with op templates, but they were all filtered out. In that case,
 * the user should be informed of an error.
 */
public interface SyntheticOpTemplateProvider extends DriverAdapterDecorators {

    /**
     * If a driver adapter supports creating example op templates from bindings,
     * it must implement this method to do so.
     *
     * @param opsDocList
     *     The existing doc structure, which should contain no fully defined op templates, but may contain other
     *     elements like bindings
     * @return A list of op templates, size zero or more
     */
    List<OpTemplate> getSyntheticOpTemplates(OpsDocList opsDocList, Map<String, Object> params);
}
