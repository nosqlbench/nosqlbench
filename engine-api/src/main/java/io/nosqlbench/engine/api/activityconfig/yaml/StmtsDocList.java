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

import io.nosqlbench.engine.api.activityconfig.rawyaml.RawScenarios;
import io.nosqlbench.engine.api.activityconfig.rawyaml.RawStmtsDocList;
import io.nosqlbench.engine.api.util.TagFilter;


import java.util.*;
import java.util.stream.Collectors;

public class StmtsDocList implements Iterable<StmtsDoc> {

    private RawStmtsDocList rawStmtsDocList;

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

    public List<StmtDef> getStmts() {
        return getStmts("");
    }

    /**
     * @return The list of all included statements for all included blocks of  in this document,
     * including the inherited and overridden values from the this doc and the parent block.
     * @param tagFilterSpec a comma-separated tag filter spec
     */
    public List<StmtDef> getStmts(String tagFilterSpec) {
        TagFilter ts = new TagFilter(tagFilterSpec);

        List<StmtDef> stmts = getStmtDocs().stream()
                .flatMap(d -> d.getStmts().stream())
                .filter(ts::matchesTagged)
                .collect(Collectors.toList());
        return stmts;
    }


    @Override
    public Iterator<StmtsDoc> iterator() {
        return getStmtDocs().iterator();
    }

    /**
     * Return the list of all bindings combined across all docs, not including
     * the block or statement level bindings.
     * @return A map of all bindings at the doc level.
     */
    public Map<String,String> getDocBindings() {
        LinkedHashMap<String,String> docBindings= new LinkedHashMap<>();
        getStmtDocs().stream()
                .map(StmtsDoc::getBindings)
                .forEach(docBindings::putAll);
        return docBindings;
    }

    /**
     * This returns all the `scenarios` blocs across multiple docs, per the description in issue-67 there should only be one
     * on the first doc, any `scenarios` defined in different docs will be ignored.
     */

    public Scenarios getDocScenarios() {
        return this.getStmtDocs().get(0).getScenarios();
    }

}
