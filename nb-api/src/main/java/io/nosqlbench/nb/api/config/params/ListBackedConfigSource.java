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

package io.nosqlbench.nb.api.config.params;

import java.util.ArrayList;
import java.util.List;

public class ListBackedConfigSource implements ConfigSource {

    private String name;

    @Override
    public boolean canRead(Object source) {
        return (source instanceof List);
    }

    @Override
    public List<ElementData> getAll(String name, Object source) {
        this.name = name;
        List<ElementData> data = new ArrayList<>();
        for (Object o : (List) source) {
            data.add(DataSources.element(o));
        }
        return data;
    }

    @Override
    public String getName() {
        return name;
    }
}
