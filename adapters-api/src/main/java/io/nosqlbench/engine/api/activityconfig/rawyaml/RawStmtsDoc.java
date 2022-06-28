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

package io.nosqlbench.engine.api.activityconfig.rawyaml;

import io.nosqlbench.engine.api.util.AdaptersApiVersionInfo;
import io.nosqlbench.nb.api.errors.OpConfigError;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A statements doc can have both a list of statement blocks and/or a
 * list of statements. It can also have all the block parameters
 * assignable to {@link RawStmtFields}.
 * <p>
 * The reason for having support both statements or statement blocks
 * is merely convenience. If you do not need or want to deal with the
 * full blocks format, the extra structure gets in the way.
 */
public class RawStmtsDoc extends StatementsOwner {

    private RawScenarios scenarios = new RawScenarios();
    private final List<RawStmtsBlock> blocks = new ArrayList<>();
    private String versionRegex = ".+";

    // no-args ctor is required
    public RawStmtsDoc() {
    }

    public static RawStmtsDoc forSingleStatement(String statement) {
        RawStmtsDoc rawStmtsDoc = new RawStmtsDoc();
        rawStmtsDoc.setStatementsFieldByType(statement);
        return rawStmtsDoc;
    }

    public void setFieldsByReflection(Map<String, Object> properties) {
        if (properties.containsKey("version_regex")) {
            String versionRegex = properties.remove("version_regex").toString();
            if (versionRegex!=null) {
                Pattern versionpattern = Pattern.compile(versionRegex);
                String version = new AdaptersApiVersionInfo().getVersion();
                if (!versionpattern.matcher(version).matches()) {
                    throw new OpConfigError("Unable to load yaml with this version '" + version + " since " +
                        "the required version doesn't match version_regex '" + versionRegex + "' from yaml.");
                }
            }
        }
        Object blocksObjects = properties.remove("blocks");
        if (blocksObjects instanceof List) {
            List<Object> blockList = ((List<Object>) blocksObjects);
            for (Object blockData : blockList) {
                if (blockData instanceof Map) {
                    Map<String, Object> blockDataMap = (Map<String, Object>) blockData;
                    RawStmtsBlock rawStmtsBlock = new RawStmtsBlock();
                    rawStmtsBlock.setFieldsByReflection(blockDataMap);
                    blocks.add(rawStmtsBlock);
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
                    RawStmtsBlock rawStmtsBlock = new RawStmtsBlock();
                    rawStmtsBlock.setName(blockName);
                    rawStmtsBlock.setFieldsByReflection(blockDataMap);
                    blocks.add(rawStmtsBlock);
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
     * Return the list of statement blocks in this RawStmtsDoc.
     * If raw statements are defined on this RawStmtsDoc, then a single
     * StmtBlock containing those statements is prepended to the block list.
     * Otherwise, the list of StmtBlocks is returned as-is.
     *
     * @return all logical statement blocks containing statements
     */
    public List<RawStmtsBlock> getBlocks() {
        List<RawStmtsBlock> stmtBlocks = new ArrayList<>();
        if (!getRawStmtDefs().isEmpty()) {
            RawStmtsBlock rawStmtsBlock = new RawStmtsBlock();
            rawStmtsBlock.setName("block0");
            rawStmtsBlock.setRawStmtDefs(getRawStmtDefs());
            stmtBlocks.add(rawStmtsBlock);
        }
        stmtBlocks.addAll(this.blocks);
        return stmtBlocks;
    }

    public void setBlocks(List<RawStmtsBlock> blocks) {
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
