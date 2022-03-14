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

package io.nosqlbench.nb.api.content;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NBIORelativizer {

    public static List<Path> relativizePaths(Path base, List<Path> contained) {
        List<Path> relativized = new ArrayList<>();
        for (Path path : contained) {
            Path relative = base.relativize(path);
            relativized.add(relative);
        }
        return relativized;
    }

    public static List<Path> relativizeContent(Path base, List<Content<?>> contained) {
        return relativizePaths(
            base,
            contained.stream().map(Content::asPath).collect(Collectors.toList()));
    }

}
