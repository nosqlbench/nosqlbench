/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.nb.api.nbio;

import io.nosqlbench.nb.api.nbio.NBIO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResolverChain {

    public enum Link {
        ALL, LOCAL, CP, FILE, CACHE
    }

    private List<Link> chains;
    private String path;

    public ResolverChain(String filepath) {
        chains = new ArrayList<>();

        String[] parts = filepath.split(":",2);

        if (parts.length < 2) {
            path = filepath;
            chains.add(Link.ALL);
        }
        for (String chain : parts[0].split("\\+")) {
            try {
                chains.add(Link.valueOf(chain.toUpperCase()));
                path = filepath.substring(parts[0].length()+1);
            } catch (IllegalArgumentException e) {
                path = filepath;
                chains.add(Link.ALL);
                break;
            }
        }
    }

    public String getPath() {
        return path;
    }

    public List<Link> getChain() {
        return chains;
    }
}
