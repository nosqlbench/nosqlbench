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

package io.nosqlbench.cqlgen.binders;

public class Binding {
    String name;
    String recipe;

    public Binding(String name, String recipe) {
        this.name = name;
        this.recipe = recipe;
    }

    public Binding withPreFunctions(String... prefixes) {
        StringBuilder sb = new StringBuilder();
        for (String prefix : prefixes) {
            String toAdd=prefix.trim();
            if (!toAdd.endsWith(";")) {
                toAdd+=";";
            }
            sb.append(toAdd);
        }
        sb.append(recipe);
        return new Binding(getName(),sb.toString());
    }

    public String getRecipe() {
        return recipe;
    }

    public String getName() {
        return name;
    }

    public Binding withNameIncrement(long l) {
        return new Binding(name+ l,recipe);
    }
}
