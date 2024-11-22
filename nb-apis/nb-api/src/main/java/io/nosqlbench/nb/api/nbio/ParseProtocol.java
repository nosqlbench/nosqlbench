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

public class ParseProtocol {

    private List<String> protocols;
    private String path;

    public ParseProtocol(String filepath) {
        protocols = new ArrayList<>();

        if (filepath.startsWith("http")) {
            path = filepath;
            protocols.add("all");
        } else {
            String[] parts = filepath.split(":");

            if (parts.length < 2) {
                path = filepath;
                protocols.add("all");
            } else {
                path = parts[1];
                protocols = Arrays.asList(parts[0].split(","));
            }
        }
    }

    public String getPath() {
        return path;
    }

    public List<String> getProtocols() {
        return protocols;
    }
}
