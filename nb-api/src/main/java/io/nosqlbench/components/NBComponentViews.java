/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.components;

import java.util.function.Function;

public class NBComponentViews {
    public static String treeView(NBComponent node) {
        return treeView(new StringBuilder(), node, 0, Object::toString);
    }

    public static String treeView(NBComponent node, Function<NBComponent,String> representer) {
        return treeView(new StringBuilder(), node, 0, representer);
    }
    private static String treeView(StringBuilder sb, NBComponent node, int level, Function<NBComponent,String> stringify) {
        sb.append(" ".repeat(level)).append(stringify.apply(node)).append("\n");
        for (NBComponent child : node.getChildren()) {
            treeView(sb,child,level+1,stringify);
        }
        return sb.toString();
    }
}
