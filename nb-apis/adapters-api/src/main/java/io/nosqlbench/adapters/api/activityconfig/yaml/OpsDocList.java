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

import io.nosqlbench.adapters.api.activityconfig.OpsLoader;
import io.nosqlbench.adapters.api.activityconfig.rawyaml.RawOpsDoc;
import io.nosqlbench.adapters.api.activityconfig.rawyaml.RawOpsDocList;
import io.nosqlbench.nb.api.tagging.TagFilter;
import io.nosqlbench.nb.api.config.standard.ConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfigModelExpander;
import io.nosqlbench.nb.api.config.standard.Param;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class OpsDocList implements Iterable<OpsDoc> {
    private final static Logger logger = LogManager.getLogger(OpsDocList.class);

    private final RawOpsDocList rawOpsDocList;
    private final Map<String, String> templateVariables = new LinkedHashMap<>();

    public OpsDocList(RawOpsDocList rawOpsDocList) {
        this.rawOpsDocList = rawOpsDocList;
//        this.applyModifier(new enumerator());
    }

    private OpsDocList(RawOpsDocList rawOpsDocList, Map<String, String> templateVariables) {
        this.rawOpsDocList = rawOpsDocList;
        this.templateVariables.putAll(templateVariables);
    }

    public static OpsDocList none() {
        return new OpsDocList(RawOpsDocList.none());
    }

    public List<OpsDoc> getStmtDocs(String tagFilter) {
        TagFilter tf = new TagFilter(tagFilter);
        return getStmtDocs().stream()
            .filter(tf::matchesTagged)
            .collect(Collectors.toList());
    }

    public List<OpsDoc> getStmtDocs() {
        return rawOpsDocList.getOpsDocs().stream()
            .map(OpsDoc::new)
            .collect(Collectors.toList());
    }

    public OpTemplates getOps() {
        return new OpTemplates(this);
    }

    @Override
    public Iterator<OpsDoc> iterator() {
        return getStmtDocs().iterator();
    }

    /**
     * Return the list of all bindings combined across all docs, not including
     * the block or statement level bindings.
     *
     * @return A map of all bindings at the doc level.
     */
    public Map<String, String> getDocBindings() {
        LinkedHashMap<String, String> docBindings = new LinkedHashMap<>();
        getStmtDocs().stream()
            .map(OpsDoc::getBindings)
            .forEach(docBindings::putAll);
        return docBindings;
    }

    /**
     * This returns all the `scenarios` blocs across multiple docs, per the description in issue-67 there should only be one
     * on the first doc, any `scenarios` defined in different docs will be ignored.
     */

    /**
     * @return the list of named scenarios for the first document in the list.
     */
    public Scenarios getDocScenarios() {
        if (this.getStmtDocs().size() == 0) {
            throw new RuntimeException("No statement docs were found, so source file is empty.");
        }
        return this.getStmtDocs().get(0).getScenarios();
    }

    /**
     * @return the description of the first document in the list.
     */
    public String getDescription() {
        return this.getStmtDocs().get(0).getDescription();
    }

    public Map<String, String> getTemplateVariables() {
        return templateVariables;
    }

    public void addTemplateVariable(String key, String defaultValue) {
        this.templateVariables.put(key, defaultValue);
    }

    public NBConfigModel getConfigModel() {
        ConfigModel cfgmodel = ConfigModel.of(OpsDocList.class);
        getTemplateVariables().forEach((k, v) -> {
            cfgmodel.add(Param.defaultTo(k, v, "template parameter found in the yaml workload"));
        });
        return cfgmodel.asReadOnly();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int docscount = 0;
        int blockscount = 0;
        int opscount = 0;

        for (OpsDoc stmtDoc : this.getStmtDocs()) {
            docscount++;
            for (OpsBlock block : stmtDoc.getBlocks()) {
                blockscount++;
                for (OpTemplate op : block.getOps()) {
                    opscount++;
                }
            }
        }

        sb.append("docs: " + docscount + " blocks:" + blockscount + " ops:" + opscount);
        return sb.toString();
    }

    public static NBConfigModelExpander TEMPLATE_VAR_EXPANDER = workload -> {
        OpsDocList loaded = OpsLoader.loadPath((String) workload, Map.of(), "activities");
        return loaded.getConfigModel();
    };

    public Pattern getVersionRegex() {
        List<RawOpsDoc> stmtDocs = rawOpsDocList.getOpsDocs();
        return Pattern.compile(stmtDocs.size()>0 ? stmtDocs.get(0).getVersionRegex() : ".*");
    }

    public int applyModifier(Consumer<OpTemplate> modifier) {
        int count=0;
        for (OpsDoc stmtDoc : this.getStmtDocs()) {
            for (OpsBlock opTemplates : stmtDoc) {
                for (OpTemplate opTemplate : opTemplates) {
                    modifier.accept(opTemplate);
                    count++;
                }
            }
        }
        return count;
    }

    public OpsDocList and(OpsDocList other) {
        return new OpsDocList(
            RawOpsDocList.combine(this.rawOpsDocList,other.rawOpsDocList),
            new LinkedHashMap<>() {{
                putAll(templateVariables);
                putAll(other.templateVariables);
            }}
        );
    }
}
