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

package io.nosqlbench.engine.core.lifecycle.scenario.script.bindings;

import javax.script.Bindings;
import java.util.Map;

/** A convenience class to make read-only Bindings easier to write.
 * This will not make access to the context efficient, but it will make it easier to do correctly.
 * More advanced implementations are recommended when the cost of indirecting through a map on access is too high.
 */
public abstract class ReadOnlyBindings implements Bindings {

    @Override
    public Object put(String name, Object value) {
        throw new ReadOnlyBindingsException(this, "put");
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> toMerge) {
        throw new ReadOnlyBindingsException(this,"putAll");
    }

    @Override
    public void clear() {
        throw new ReadOnlyBindingsException(this,"clear");
    }


    @Override
    public Object remove(Object key) {
        throw new ReadOnlyBindingsException(this, "remove");
    }

}
