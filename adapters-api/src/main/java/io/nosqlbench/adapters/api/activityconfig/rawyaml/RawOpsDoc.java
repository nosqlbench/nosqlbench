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

import io.nosqlbench.adapters.api.util.AdaptersApiVersionInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RawOpsDoc extends OpsOwner {

    private RawScenarios scenarios = new RawScenarios();
    private final List<RawOpsBlock> blocks = new ArrayList<>();
    private String versionRegex = ".+";

    // no-args ctor is required
    public RawOpsDoc() {
    }

    public static RawOpsDoc forSingleStatement(String statement) {
        RawOpsDoc rawOpsDoc = new RawOpsDoc();
        rawOpsDoc.setOpsFieldByType(statement);
        return rawOpsDoc;
    }

    public void setFieldsByReflection(Map<String, Object> properties) {
        if (properties.containsKey("version_regex")) {
            String versionRegex = properties.remove("version_regex").toString();
            new AdaptersApiVersionInfo().assertVersionPattern(versionRegex);
        }
        if (properties.containsKey("min_version")) {
            String min_version = properties.remove("min_version").toString();
            new AdaptersApiVersionInfo().assertNewer(min_version);
        }

        Object blocksObjects = properties.remove("blocks");
        if (blocksObjects instanceof List) {
            List<Object> blockList = ((List<Object>) blocksObjects);
            for (Object blockData : blockList) {
                if (blockData instanceof Map) {
                    Map<String, Object> blockDataMap = (Map<String, Object>) blockData;
                    RawOpsBlock rawOpsBlock = new RawOpsBlock();
                    rawOpsBlock.setFieldsByReflection(blockDataMap);
                    blocks.add(rawOpsBlock);
                } else {
                    throw new RuntimeException("Invalid object type for block data: " + blockData.getClass().getCanonicalName());
                }
            }
        } else if (blocksObjects instanceof Map) {
            Map<String, Object> blockDataAsMap = (Map<String, Object>) blocksObjects;
            for (Map.Entry<String, Object> entry : blockDataAsMap.entrySet()) {
                String blockName = entry.getKey();
                Object blockData = entry.getValue();
                if (blockData instanceof Map) {
                    Map<String, Object> blockDataMap = (Map<String, Object>) blockData;
                    RawOpsBlock rawOpsBlock = new RawOpsBlock();
                    rawOpsBlock.setName(blockName);
                    rawOpsBlock.setFieldsByReflection(blockDataMap);
                    blocks.add(rawOpsBlock);
                } else {
                    throw new RuntimeException("Invalid object type for block data: " + blockData.getClass().getCanonicalName());
                }

            }
        } else if (blocksObjects != null) {
            throw new RuntimeException("Type of blocks interior data type not recognized:" + blocksObjects.getClass().getCanonicalName());
        }

        Object scenariosData = properties.remove("scenarios");
        if (scenariosData != null) {
            scenarios.setPropertiesByReflection(scenariosData);
        }

        super.setFieldsByReflection(properties);

    }

    /**
     * If raw ops are defined on this RawOpsDoc, then a single RawOpsBlock containing those op templates is prepended to
     * the block list. Otherwise, the list of RawOpsBlocks is returned as-is.
     *
     * @return all logical ops blocks
     */
    public List<RawOpsBlock> getBlocks() {
        List<RawOpsBlock> stmtBlocks = new ArrayList<>();
        if (!getRawOpDefs().isEmpty()) {
            RawOpsBlock rawOpsBlock = new RawOpsBlock();
            rawOpsBlock.setName("block0");
            rawOpsBlock.setRawStmtDefs(getRawOpDefs());
            stmtBlocks.add(rawOpsBlock);
        }
        stmtBlocks.addAll(this.blocks);
        return stmtBlocks;
    }

    public void setBlocks(List<RawOpsBlock> blocks) {
        this.blocks.clear();
        this.blocks.addAll(blocks);
    }

    public RawScenarios getRawScenarios() {
        return this.scenarios;
    }

    public void setScenarios(RawScenarios scenarios) {
        this.scenarios = scenarios;
    }

    public String getVersionRegex() {
        return this.versionRegex;
    }

    public void setVersionRegex(String regex) {
        this.versionRegex = regex;
    }

}
