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

package io.nosqlbench.adapters.api.activityconfig.yaml;

import io.nosqlbench.adapters.api.activityconfig.rawyaml.RawOpsBlock;
import io.nosqlbench.adapters.api.activityconfig.rawyaml.RawOpsDoc;
import io.nosqlbench.api.engine.util.Tagged;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * OpsDoc creates a logical view of a RawOpsDoc doc that includes
 * all inherited and overridden values for bindings, tags, and params.
 */
public class OpsDoc implements Tagged, Iterable<OpsBlock> {

    private final RawOpsDoc rawOpsDoc;

    public OpsDoc(RawOpsDoc rawOpsDoc) {
        this.rawOpsDoc = rawOpsDoc;
    }

    /**
     * @return a usable list of blocks, including inherited bindings, params, and tags
     * from the parent doc
     */
    public List<OpsBlock> getBlocks() {
        List<OpsBlock> blocks = new ArrayList<>();

        int blockIdx = 0;
        for (RawOpsBlock rawOpsBlock : rawOpsDoc.getBlocks()) {
            String compositeName = rawOpsDoc.getName() +
                    (rawOpsBlock.getName().isEmpty() ? "" : "-" + rawOpsBlock.getName());
            OpsBlock compositeBlock = new OpsBlock(rawOpsBlock, this, ++blockIdx);
            blocks.add(compositeBlock);
        }

        return blocks;
    }

    /**
     * @return a usable map of tags, including those inherited from the parent doc
     */
    @Override
    public Map<String, String> getTags() {
        return rawOpsDoc.getTags();
    }

    /**
     * @return a usable map of parameters, including those inherited from the parent doc
     */
    public Map<String, Object> getParams() {
        return rawOpsDoc.getParams();
    }

    /**
     * @return a usable map of bindings, including those inherited from the parent doc
     */
    public Map<String, String> getBindings() {
        return rawOpsDoc.getBindings();
    }

    /**
     * @return the name of this block
     */
    public String getName() {
        return rawOpsDoc.getName();
    }

    /**
     * @return The list of all included op templates for all included block in this document,
     * including the inherited and overridden values from this doc and the parent block.
     */
    public List<OpTemplate> getOpTemplates() {
        return getBlocks().stream().flatMap(b -> b.getOps().stream()).collect(Collectors.toList());
    }

    /**
     * Allow StmtsDoc to be used in iterable loops.
     * @return An iterator of {@link OpsBlock}
     */
    @Override
    public Iterator<OpsBlock> iterator() {
        return getBlocks().iterator();
    }


    public Scenarios getScenarios() {
        return new Scenarios(rawOpsDoc.getRawScenarios());
    }

    public String getDescription() {
        return rawOpsDoc.getDesc();
    }
}
