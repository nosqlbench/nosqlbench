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

package io.nosqlbench.engine.api.activityconfig.yaml;

import io.nosqlbench.engine.api.activityconfig.StatementsLoader;
import io.nosqlbench.engine.api.activityconfig.rawyaml.RawStmtsDocList;
import io.nosqlbench.engine.api.util.TagFilter;
import io.nosqlbench.nb.api.config.standard.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class StmtsDocList implements Iterable<StmtsDoc> {
    private final static Logger logger = LogManager.getLogger(StmtsDocList.class);

    private final RawStmtsDocList rawStmtsDocList;
    private final Map<String, String> templateVariables = new LinkedHashMap<>();

    public StmtsDocList(RawStmtsDocList rawStmtsDocList) {
        this.rawStmtsDocList = rawStmtsDocList;
    }

    public List<StmtsDoc> getStmtDocs(String tagFilter) {
        TagFilter tf = new TagFilter(tagFilter);
        return getStmtDocs().stream()
            .filter(tf::matchesTagged)
            .collect(Collectors.toList());
    }

    public List<StmtsDoc> getStmtDocs() {
        return rawStmtsDocList.getStmtsDocs().stream()
            .map(StmtsDoc::new)
            .collect(Collectors.toList());
    }

    public List<OpTemplate> getStmts() {
        return getStmts("");
    }

    /**
     * @param tagFilterSpec a comma-separated tag filter spec
     * @return The list of all included statements for all included blocks of  in this document,
     * including the inherited and overridden values from the this doc and the parent block.
     */
    public List<OpTemplate> getStmts(String tagFilterSpec) {
        TagFilter ts = new TagFilter(tagFilterSpec);
        List<OpTemplate> opTemplates = new ArrayList<>();


        getStmtDocs().stream()
            .flatMap(d -> d.getStmts().stream())
            .filter(ts::matchesTagged)
            .forEach(opTemplates::add);

        return opTemplates;
    }


    @Override
    public Iterator<StmtsDoc> iterator() {
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
            .map(StmtsDoc::getBindings)
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
        ConfigModel cfgmodel = ConfigModel.of(StmtsDocList.class);
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

        for (StmtsDoc stmtDoc : this.getStmtDocs()) {
            docscount++;
            for (StmtsBlock block : stmtDoc.getBlocks()) {
                blockscount++;
                for (OpTemplate op : block.getOps()) {
                    opscount++;
                }
            }
        }

        sb.append("docs: " + docscount + " blocks:" + blockscount + " ops:" + opscount);
//        String names = this.rawStmtsDocList.getStmtsDocs().stream().flatMap(sd -> sd.getRawStmtDefs().stream()).map(d->d.getName()).collect(Collectors.joining(","));
//        sb.append(", names:").append(names);
        return sb.toString();
    }

    public static NBConfigModelExpander TEMPLATE_VAR_EXPANDER = workload -> {
        StmtsDocList loaded = StatementsLoader.loadPath(logger, (String) workload, "activities");
        return loaded.getConfigModel();
    };
}
