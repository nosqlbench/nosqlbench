/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.engine.api.activityconfig.yaml;

import io.nosqlbench.engine.api.activityconfig.rawyaml.RawStmtsBlock;
import io.nosqlbench.engine.api.activityconfig.rawyaml.RawStmtsDoc;
import io.nosqlbench.engine.api.util.Tagged;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * StmtsDoc creates a logical view of a statements doc that includes
 * all inherited and overridden values for bindings, tags, and params.
 */
public class StmtsDoc implements Tagged, Iterable<StmtsBlock> {

    private RawStmtsDoc rawStmtsDoc;

    public StmtsDoc(RawStmtsDoc rawStmtsDoc) {
        this.rawStmtsDoc = rawStmtsDoc;
    }

    /**
     * @return a usable list of blocks, including inherited bindings, params, and tags
     * from the parent doc
     */
    public List<StmtsBlock> getBlocks() {
        List<StmtsBlock> blocks = new ArrayList<>();

        int blockIdx = 0;
        for (RawStmtsBlock rawStmtsBlock : rawStmtsDoc.getBlocks()) {
            String compositeName = rawStmtsDoc.getName() +
                    (rawStmtsBlock.getName().isEmpty() ? "" : "-" + rawStmtsBlock.getName());
            StmtsBlock compositeBlock = new StmtsBlock(rawStmtsBlock, this, ++blockIdx);
            blocks.add(compositeBlock);
        }

        return blocks;
    }

    /**
     * @return a usable map of tags, including those inherited from the parent doc
     */
    @Override
    public Map<String, String> getTags() {
        return rawStmtsDoc.getTags();
    }

    /**
     * @return a usable map of parameters, including those inherited from the parent doc
     */
    public Map<String, String> getParams() {
        return rawStmtsDoc.getParams();
    }

    /**
     * @return a usable map of bindings, including those inherited from the parent doc
     */
    public Map<String, String> getBindings() {
        return rawStmtsDoc.getBindings();
    }

    /**
     * @return the name of this block
     */
    public String getName() {
        return rawStmtsDoc.getName();
    }

    /**
     * @return The list of all included statements for all included block in this document,
     * including the inherited and overridden values from the this doc and the parent block.
     */
    public List<StmtDef> getStmts() {
        return getBlocks().stream().flatMap(b -> b.getStmts().stream()).collect(Collectors.toList());
    }

    /**
     * Allow StmtsDoc to be used in iterable loops.
     * @return An iterator of {@link StmtsBlock}
     */
    @Override
    public Iterator<StmtsBlock> iterator() {
        return getBlocks().iterator();
    }


    public Scenarios getScenarios() {
        return new Scenarios(rawStmtsDoc.getRawScenarios());
    }

    public String getDescription() {
        return rawStmtsDoc.getDesc();
    }
}
