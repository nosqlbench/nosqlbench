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

package io.nosqlbench.adapters.api.activityconfig.rawyaml;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class RawOpsDocList implements Iterable<RawOpsDoc> {

    private final List<RawOpsDoc> rawOpsDocList;

    public RawOpsDocList(List<RawOpsDoc> rawOpsDocList) {
        this.rawOpsDocList = rawOpsDocList;
    }

    public static RawOpsDocList forSingleStatement(String statement) {
        RawOpsDoc rawOpsDoc = RawOpsDoc.forSingleStatement(statement);
        return new RawOpsDocList(List.of(rawOpsDoc));
    }

    public static RawOpsDocList none() {
        return new RawOpsDocList(List.of());
    }

    public List<RawOpsDoc> getOpsDocs() {
        return rawOpsDocList;
    }

    public String toString() {
        int docs = rawOpsDocList.size();
        int blocks = rawOpsDocList.stream().map(RawOpsDoc::getBlocks).mapToInt(List::size).sum();
        long optemplates = rawOpsDocList.stream().flatMap(d -> d.getBlocks().stream()).flatMap(s -> s.getRawOpDefs().stream()).count();
        return "docs:" + docs + " blocks:" + blocks + " optemplates:" + optemplates;
    }

    @Override
    public @NotNull Iterator<RawOpsDoc> iterator() {
        return this.rawOpsDocList.iterator();
    }

    public int applyModifier(Consumer<RawOpDef> modifier) {
        int count = 0;
        for (RawOpsDoc rawOpsDoc : rawOpsDocList) {
            for (RawOpsBlock opBlock : rawOpsDoc) {
                for (RawOpDef rawOpDef : opBlock.getRawOpDefs()) {
                    modifier.accept(rawOpDef);
                    count++;
                }
            }
        }
        return count;
    }
}
