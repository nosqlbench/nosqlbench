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

package io.nosqlbench.engine.api.activityconfig.rawyaml;

import java.util.List;

public class RawStmtsDocList {

    private final List<RawStmtsDoc> rawStmtsDocList;

    public RawStmtsDocList(List<RawStmtsDoc> rawStmtsDocList) {
        this.rawStmtsDocList = rawStmtsDocList;
    }

    public static RawStmtsDocList forSingleStatement(String statement) {
        RawStmtsDoc rawStmtsDoc = RawStmtsDoc.forSingleStatement(statement);
        return new RawStmtsDocList(List.of(rawStmtsDoc));
    }

    public static RawStmtsDocList none() {
        return new RawStmtsDocList(List.of());
    }

    public List<RawStmtsDoc> getStmtsDocs() {
        return rawStmtsDocList;
    }

    public String toString() {
        int docs = rawStmtsDocList.size();
        int blocks = rawStmtsDocList.stream().map(RawStmtsDoc::getBlocks).mapToInt(List::size).sum();
        long optemplates = rawStmtsDocList.stream().flatMap(d -> d.getBlocks().stream()).flatMap(s -> s.getRawStmtDefs().stream()).count();
        return "docs:" + docs + " blocks:" + blocks + " optemplates:" + optemplates;
    }
}
