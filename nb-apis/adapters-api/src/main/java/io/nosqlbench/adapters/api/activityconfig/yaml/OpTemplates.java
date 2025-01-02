package io.nosqlbench.adapters.api.activityconfig.yaml;

/*
 * Copyright (c) nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.adapters.api.activityconfig.rawyaml.RawOpsDocList;
import io.nosqlbench.nb.api.tagging.TagFilter;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

/// [OpTemplates] is a list of selected op templates and their backing data.
///
/// It is a value type which makes it easy to /// get matching subsets of op templates according to tag filters, to combine them, etc.
///
/// When a user selects an op template, they are expected to use the [TagFilter] mechanism.
/// Any such lookup methods should be implemented on this class.
public class OpTemplates implements Iterable<OpTemplate>  {
    private final ArrayList<OpTemplate> templates = new ArrayList<>();
    private final static Logger logger = LogManager.getLogger(OpTemplates.class);
    private final OpsDocList opsDocList;

    public OpTemplates(OpsDocList opsDocList) {
        opsDocList.getStmtDocs().stream().flatMap(d -> d.getOpTemplates().stream()).forEach(templates::add);
        this.opsDocList = opsDocList;
    }

    public OpTemplates(List<OpTemplate> matchingOpTemplates, OpsDocList opsDocList) {
        this.opsDocList = opsDocList;
        templates.addAll(matchingOpTemplates);
    }

    public OpTemplates() {
        this.opsDocList=new OpsDocList(new RawOpsDocList(List.of()));
    }

    public OpTemplates and(OpTemplates other) {
        this.opsDocList.and(opsDocList);
        return new OpTemplates();
    }

    /**
     * @param tagFilterSpec a comma-separated tag filter spec
     * @return The list of all included op templates for all included blocks of  in this document,
     * including the inherited and overridden values from this doc and the parent block.
     */
    public OpTemplates matching(String tagFilterSpec, boolean logit) {
        return matching(new TagFilter(tagFilterSpec), logit);
    }
    public OpTemplates matching(TagFilter tagFilter, boolean logit) {
        List<OpTemplate> matchingOpTemplates = new ArrayList<>();

        List<String> matchlog = new ArrayList<>();
        templates.stream()
            .map(tagFilter::matchesTaggedResult)
            .peek(r -> matchlog.add(r.getLog()))
            .filter(TagFilter.Result::matched)
            .map(TagFilter.Result::getElement)
            .forEach(matchingOpTemplates::add);

        if (logit) {
            for (String s : matchlog) {
                logger.info(s);
            }
        }

        return new OpTemplates(matchingOpTemplates,opsDocList);

    }

    public Map<String,String> getDocBindings() {
        return opsDocList.getDocBindings();
    }

    @Override
    public @NotNull Iterator<OpTemplate> iterator() {
        return templates.iterator();
    }

    public Stream<OpTemplate> stream() {
        return templates.stream();
    }

    public int size() {
        return templates.size();
    }

    public OpTemplate get(int idx) {
        return templates.get(idx);
    }

    public boolean isEmpty() {
        return this.templates.isEmpty();
    }

    public OpTemplates transform(Function<OpTemplate,OpTemplate> transformF) {
        List<OpTemplate> transformed = this.templates.stream().map(t -> transformF.apply(t)).toList();
        return new OpTemplates(transformed,opsDocList);
    }

}
